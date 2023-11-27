package com.mgmtp.gradle.libactivity.plugin.checker.lib

import com.mgmtp.gradle.libactivity.plugin.checker.config.LocalConfigChecker
import com.mgmtp.gradle.libactivity.plugin.config.GlobalConfig
import com.mgmtp.gradle.libactivity.plugin.config.LocalConfig
import com.mgmtp.gradle.libactivity.plugin.data.lib.Lib
import com.mgmtp.gradle.libactivity.plugin.data.lib.MavenIdentifier
import com.mgmtp.gradle.libactivity.plugin.logging.LazyLogger
import com.mgmtp.gradle.libactivity.plugin.result.data.CheckResult
import com.mgmtp.gradle.libactivity.plugin.result.data.CheckResultGroup
import com.mgmtp.gradle.libactivity.plugin.result.data.lib.LibCheckResult
import com.mgmtp.gradle.libactivity.plugin.result.format.collector.FormattedCheckResultCollectorFactory
import com.mgmtp.gradle.libactivity.plugin.result.format.formatter.CheckResultFormatterFactory
import com.mgmtp.gradle.libactivity.plugin.result.writer.CheckResultWriterFactory
import com.mgmtp.gradle.libactivity.plugin.util.NullCheck
import groovy.json.JsonSlurper
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

import java.time.LocalDate
import java.util.concurrent.ThreadLocalRandom

@TupleConstructor(post = { NullCheck.ALL_PROPS.call(this) })
@VisibilityOptions(Visibility.PRIVATE)
class LibChecker {

    final GlobalConfig globalConfig

    final LocalConfig localConfig

    final LocalConfigChecker localConfigChecker

    private final OkHttpClient okHttpClient = new OkHttpClient()

    private static final String HTTPS_SCHEME = 'https'

    private static final LazyLogger LOGGER = LazyLogger.fromClazz(LibChecker.class)

    private static final Closure<String> GENERIC_REQUEST_LOG = { HttpUrl url -> "Sending request: '${url}'." }

    private static final Closure<String> GENERIC_INVALID_RESPONSE_LOG = { int responseCode, Lib lib, String url ->
        "Received invalid response '${responseCode}' when checking lib '${lib}' via URL: '${url}'."
    }

    static LibChecker fromConfigBundle(GlobalConfig globalConfig, LocalConfig localConfig) {
        return new LibChecker(globalConfig, localConfig, LocalConfigChecker.fromLocalConfig(localConfig))
    }

    <GM extends Enum<GM>, M, G extends CheckResultGroup<GM, M>> void checkLibMavenIdentifiers(Collection<MavenIdentifier> libMavenIdentifiers) {

        LOGGER.info('Starting lib check.')

        LOGGER.info { "Checking if any of ${libMavenIdentifiers.size()} libs are meant to be xcluded." }
        List<Lib> libs = libMavenIdentifiers.collect { MavenIdentifier mavenIdentifier -> Lib.fromMavenIdentifier(mavenIdentifier) }
                .findAll { Lib lib -> isNotXcludedLib(lib) }
        LOGGER.info { "${libs.size()} libs passed xclusion filter." }

        libs.each { Lib lib -> tagLib(lib) }
        List<CheckResult<GM, M, G>> checkResults = collectNonEmptyCheckResults(LibCheckResult.fromTaggedLibs(libs), localConfigChecker.result)

        if (checkResults) {
            CheckResultWriterFactory.getWriter(localConfig).write(getWritableCheckResults(checkResults))
        } else {
            LOGGER.info('No check results.')
        }
    }

    /** xcludes are check prior to xcludePatterns. */
    private boolean isNotXcludedLib(Lib lib) {
        String groupArtifactTuple = lib.mavenIdentifier.groupId + ':' + lib.mavenIdentifier.artifactId
        Optional<String> matchedXcludePattern = Optional.empty()
        Optional<String> matchedXcludeItem = localConfig.xcludes.stream()
                .filter { String xclude ->
                    LOGGER.debug("Checking lib '{}' against Xclude '{}'", lib, xclude)
                    xclude == groupArtifactTuple
                }
                .findFirst()
        matchedXcludeItem.ifPresent { String xclude -> localConfigChecker.markXcludeAsUsed(xclude) }
        if (!matchedXcludeItem) {
            matchedXcludePattern = localConfig.xcludePatterns.stream()
                    .filter { String pattern ->
                        LOGGER.debug("Checking lib '{}' against Xclude Pattern '{}'", lib, pattern)
                        groupArtifactTuple.matches(pattern)
                    }
                    .findFirst()
            matchedXcludePattern.ifPresent { String pattern -> localConfigChecker.markXcludePatternAsUsed(pattern) }
        }
        return !matchedXcludeItem && !matchedXcludePattern
    }

    /** Tag every lib based on Sonatype response. If release outdated collect more tags. */
    private void tagLib(Lib lib) {
        tagLibViaSonatypeQuery(lib)
        if (Lib.Tag.OUTDATED_RELEASE in lib.tags) {
            tagApparentlyInactiveLib(lib)
        }
    }

    private void tagLibViaSonatypeQuery(Lib lib) {

        LOGGER.info("Checking lib '{}' via Sonatype API.", lib)
        HttpUrl httpUrl = new HttpUrl.Builder()
                .scheme(HTTPS_SCHEME)
                .host('search.maven.org')
                .addPathSegment('solrsearch')
                .addPathSegment('select')
                .addEncodedQueryParameter('q', "g:\"${lib.mavenIdentifier.groupId}\"+AND+a:\"${lib.mavenIdentifier.artifactId}\"")
                .addQueryParameter('core', 'gav')
                .addQueryParameter('rows', '100')
                .addQueryParameter('wt', 'json')
                .build()
        LOGGER.debug { GENERIC_REQUEST_LOG.call(httpUrl) }

        try {
            Object jsonObject = getJsonResponseFromRequestToUrl(httpUrl)
            List<String> versions = jsonObject.response.docs.v
            List<Long> timestamps = jsonObject.response.docs.timestamp
            Map<String, Long> sonatypeQueryResult = [versions, timestamps].transpose()
                    .collect { Object list -> (List<?>) list }
                    .collectEntries { List<?> list -> [(list[0] as String): list[1] as Long] }
            if (!sonatypeQueryResult) {
                LOGGER.info("Lib '{}' is unknown.", lib)
                lib.tags.add(Lib.Tag.UNKNOWN)
            } else {
                LOGGER.info("Checking current version age and latest release age for lib '{}'.", lib)
                tagLibBasedOnCurrentVersionAge(lib, sonatypeQueryResult)
                tagLibBasedOnLatestReleaseAge(lib, timestamps.first())
            }
        } catch (HttpStatusException e) {
            tagLibBasedOnInvalidHttpResponse(lib, e)
        } catch (Exception e) {
            // contribute additional log in case gradle task does not run with --stacktrace
            LOGGER.error("Sonatype API request '${httpUrl}' caused error", e)
            throw new IllegalStateException(e)
        }
    }

    private void tagLibBasedOnCurrentVersionAge(Lib lib, Map<String, Long> sonatypeQueryResult) {
        if (sonatypeQueryResult.containsKey(lib.mavenIdentifier.version)) {
            LocalDate earliestPossibleVersionReleaseDate = globalConfig.startOfCheckDate.minusMonths(localConfig.maxAgeCurrentVersionInMonths)
            LocalDate libVersionReleaseDate = getLocalDateFromUnixMillis(sonatypeQueryResult[lib.mavenIdentifier.version])
            if (libVersionReleaseDate.isBefore(earliestPossibleVersionReleaseDate)) {
                LOGGER.info("Lib '{}' has outdated version.", lib)
                lib.tags.add(Lib.Tag.OUTDATED_VERSION)
                lib.details.put(Lib.Detail.VERSION_AGE, getFormattedYearsSince(libVersionReleaseDate))
            }
        } else {
            LOGGER.info("Lib '{}' has unknown version.", lib)
            lib.tags.add(Lib.Tag.UNKNOWN_VERSION)
        }
    }

    private void tagLibBasedOnLatestReleaseAge(Lib lib, long latestReleaseTimestamp) {
        LocalDate latestReleaseDate = getLocalDateFromUnixMillis(latestReleaseTimestamp)
        LocalDate earliestPossibleLatestReleaseDate = globalConfig.startOfCheckDate.minusMonths(localConfig.maxAgeLatestReleaseInMonths)
        if (latestReleaseDate.isBefore(earliestPossibleLatestReleaseDate)) {
            LOGGER.info("Lib '{}' has outdated release.", lib)
            lib.tags.add(Lib.Tag.OUTDATED_RELEASE)
            lib.details.put(Lib.Detail.LATEST_RELEASE_AGE, getFormattedYearsSince(latestReleaseDate))
        } else {
            LOGGER.info("Latest release of lib '{}' is OK.", lib)
            lib.tags.addAll(Lib.Tag.ACTIVE, Lib.Tag.RELEASE_OK)
        }
    }

    /**
     * Local GitHub mappings are preferred over global mappings. This is because if a repository has recently moved
     * the user may provide the new location via a local mapping without having to wait for a new plugin release
     * containing the updated location.
     *
     * If a local GitHub mapping is equal to a global one it will be reported redundant since there is no added value.
     */
    private void tagApparentlyInactiveLib(Lib lib) {
        String gitHubMappingKey = lib.mavenIdentifier.groupId + '/' + lib.mavenIdentifier.artifactId
        Optional<String> localMappedLibNameOpt = Optional.ofNullable(localConfig.localGitHubMappings[gitHubMappingKey])
        Optional<String> globalMappedLibNameOpt = Optional.ofNullable(globalConfig.gitHubMappings[gitHubMappingKey])
        Optional<String> mappedLibNameOpt = localMappedLibNameOpt ?: globalMappedLibNameOpt

        if (!mappedLibNameOpt) {
            LOGGER.info("Cannot check lib '{}' on GitHub. No mapping available.", lib)
            lib.tags.addAll(Lib.Tag.UNAVAILABLE_RESULT, Lib.Tag.NO_GITHUB_MAPPING)
        } else {
            String mappedLibName = mappedLibNameOpt.get()
            if (!mappedLibName) {
                LOGGER.info("Lib '{}' not hosted on GitHub.", lib)
                lib.tags.addAll(Lib.Tag.INACTIVE, Lib.Tag.NO_GITHUB_HOSTING)
                tagLibViaMvnRepositoryQuery(lib)
            } else {
                tagLibViaGitHubQuery(lib, mappedLibName)
            }
            if (localMappedLibNameOpt && localMappedLibNameOpt != globalMappedLibNameOpt) {
                localConfigChecker.markLocalGitHubMappingKeyAsUsed(gitHubMappingKey)
            }
        }
    }

    private void tagLibViaGitHubQuery(Lib lib, String mappedLibName) {

        LOGGER.info("Checking commits for apparantly inactive lib '{}' on GitHub.", lib)
        String[] mappedFragments = mappedLibName.split(':')
        LocalDate earliestPossibleLatestReleaseDate = globalConfig.startOfCheckDate.minusMonths(localConfig.maxAgeLatestReleaseInMonths)
        HttpUrl.Builder httpUrlBuilder = new HttpUrl.Builder()
                .scheme(HTTPS_SCHEME)
                .host('api.github.com')
                .addPathSegment('repos')
                .addPathSegment(mappedFragments[0])
                .addPathSegment('commits')
                .addEncodedQueryParameter('since', "${earliestPossibleLatestReleaseDate}T00:00:00Z")
        if (mappedFragments.length > 1) {
            httpUrlBuilder.addQueryParameter('path', mappedFragments[1])
        }
        HttpUrl httpUrl = httpUrlBuilder.build()
        LOGGER.debug { GENERIC_REQUEST_LOG.call(httpUrl) }

        Map<String, String> gitHubAuthorizationHeader = localConfig.gitHubOauthToken ? ['Authorization': "token ${localConfig.gitHubOauthToken}"] : [:]

        try {
            int numCommitsInTimeFrame = getJsonResponseFromRequestToUrl(httpUrl, gitHubAuthorizationHeader).size()
            if (numCommitsInTimeFrame > 0) {
                LOGGER.info("Found {} commits for '{}' on GitHub.", numCommitsInTimeFrame, lib)
                lib.tags.addAll(Lib.Tag.ACTIVE, Lib.Tag.AT_LEAST_1_COMMIT)
                lib.details.put(Lib.Detail.NUM_COMMITS, numCommitsInTimeFrame)
            } else {
                LOGGER.info("No commits found for '{}' on GitHub.", lib)
                lib.tags.addAll(Lib.Tag.INACTIVE, Lib.Tag.NO_COMMITS)
                tagLibViaMvnRepositoryQuery(lib)
            }
        } catch (HttpStatusException e) {
            tagLibBasedOnInvalidHttpResponse(lib, e)
        } catch (Exception e) {
            LOGGER.error("GitHub API request '${httpUrl}' caused error", e)
            throw new IllegalStateException(e)
        }
    }

    private static void tagLibViaMvnRepositoryQuery(Lib lib) {

        LOGGER.info("Checking move status for inactive '{}' via MvnRepository.", lib)
        HttpUrl httpUrl = new HttpUrl.Builder()
                .scheme(HTTPS_SCHEME)
                .host('mvnrepository.com')
                .addPathSegment('artifact')
                .addPathSegment(lib.mavenIdentifier.groupId)
                .addPathSegment(lib.mavenIdentifier.artifactId)
                .build()
        LOGGER.debug { GENERIC_REQUEST_LOG.call(httpUrl) }

        try {
            getNewLibAddressViaMvnRepository(httpUrl.toString()).ifPresent { newAddress ->
                LOGGER.info("Lib '{}' moved to new address: '{}'.", lib, newAddress)
                lib.tags.add(Lib.Tag.MOVED)
                lib.details.put(Lib.Detail.NEW_ADDRESS, newAddress)
            }
        } catch (HttpStatusException e) {
            LOGGER.warn { GENERIC_INVALID_RESPONSE_LOG.call(e.statusCode, lib, httpUrl) }
        } catch (Exception e) {
            LOGGER.warn("MvnRepository request '${httpUrl}' caused error", e)
        }
    }

    private <GM extends Enum<GM>, M, G extends CheckResultGroup<GM, M>> String getWritableCheckResults(Collection<CheckResult<GM, M, G>> checkResults) {
        return checkResults.stream()
                .map { CheckResult<GM, M, G> result ->
                    LOGGER.info("Formatting check result '{}'", result.name)
                    CheckResultFormatterFactory.getFormatter(localConfig.outputFormat, result.class).format(result)
                }
                .collect(FormattedCheckResultCollectorFactory.getCollector(localConfig.outputFormat))
    }

    private String getFormattedYearsSince(LocalDate localDate) {
        return String.format(Locale.US, '%.1f', localDate.until(globalConfig.startOfCheckDate).toTotalMonths() / 12.0)
    }

    /**
     * No API call. Care must be taken not to get locked out. A human user needs time to click.
     * <p>
     * If the artifact was moved then 2 links should appear in the corresponding section. The 1st one follows the group ID and the 2nd one the artifact ID.
     * We assume the last link is most useful. Links are relative / start with a slash.
     * </p>
     */
    private static Optional<String> getNewLibAddressViaMvnRepository(String url) {

        Thread.sleep(ThreadLocalRandom.current().nextInt(1000, 4001))
        List<String> newLinks = Jsoup.connect(url).userAgent('Mozilla').get().getElementsContainingOwnText('artifact was moved')
                .collect { Element element -> element.getElementsByTag('a') }.flatten()
                .collect { Object element -> ((Element) element).attr('href') }
                .findAll { String newLink -> newLink }

        if (newLinks) {
            HttpUrl.Builder httpUrlBuilder = new HttpUrl.Builder()
                    .scheme(HTTPS_SCHEME)
                    .host('mvnrepository.com')
                    .encodedPath(newLinks.last())
            return Optional.of(httpUrlBuilder.build().toString())
        }
        return Optional.empty()
    }

    private static void tagLibBasedOnInvalidHttpResponse(Lib lib, HttpStatusException httpStatusException) {
        int responseCode = httpStatusException.getStatusCode()
        String encodedUrl = httpStatusException.getUrl()
        switch (responseCode) {
            case 403:
                if (encodedUrl.contains('api.github.com')) {
                    LOGGER.warn { GENERIC_INVALID_RESPONSE_LOG.call(responseCode, lib, encodedUrl) }
                    lib.tags.addAll(Lib.Tag.UNAVAILABLE_RESULT, Lib.Tag.GITHUB_RESPONSE_403)
                    break
                }
                LOGGER.error { GENERIC_INVALID_RESPONSE_LOG.call(responseCode, lib, encodedUrl) }
                throw new IllegalStateException(httpStatusException)
            case 404:
                if (encodedUrl.contains('search.maven.org')) {
                    LOGGER.info { GENERIC_INVALID_RESPONSE_LOG.call(responseCode, lib, encodedUrl) }
                    lib.tags.add(Lib.Tag.UNKNOWN)
                } else {
                    LOGGER.warn { GENERIC_INVALID_RESPONSE_LOG.call(responseCode, lib, encodedUrl) }
                    lib.tags.addAll(Lib.Tag.UNAVAILABLE_RESULT, Lib.Tag.GITHUB_RESPONSE_404)
                }
                break
            default:
                LOGGER.error { GENERIC_INVALID_RESPONSE_LOG.call(responseCode, lib, encodedUrl) }
                throw new IllegalStateException(httpStatusException)
        }
    }

    private static LocalDate getLocalDateFromUnixMillis(long unixMillis) {
        return LocalDate.ofEpochDay(Long.divideUnsigned(unixMillis, 86400000L))
    }

    private Object getJsonResponseFromRequestToUrl(HttpUrl httpUrl) {
        return getJsonResponseFromRequestToUrl(httpUrl, [:])
    }

    private Object getJsonResponseFromRequestToUrl(HttpUrl httpUrl, Map<String, String> requestHeaderParams) {

        Request.Builder requestBuilder = new Request.Builder().url(httpUrl)
        requestHeaderParams.forEach { String key, String value -> requestBuilder.addHeader(key, value) }

        try (Response response = okHttpClient.newCall(requestBuilder.build()).execute()) {

            int responseCode = response.code()
            if (responseCode != 200) {
                throw new HttpStatusException("Request '${httpUrl}' received invalid response ${responseCode}", responseCode, httpUrl.toString())
            }
            return new JsonSlurper().parse(response.body().byteStream())
        }
    }

    private static <GM extends Enum<GM>, M, G extends CheckResultGroup<GM, M>> List<CheckResult<GM, M, G>> collectNonEmptyCheckResults(CheckResult<GM, M, G>... checkResults) {
        return checkResults.findAll { CheckResult<GM, M, G> result -> result.groups }
    }
}