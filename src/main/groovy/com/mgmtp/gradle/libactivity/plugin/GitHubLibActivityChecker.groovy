package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope
import okhttp3.HttpUrl
import org.apache.commons.lang3.Range

import javax.annotation.Nullable

@PackageScope
class GitHubLibActivityChecker implements IApiLibActivityChecker {

    private static final LazyLogger LOGGER = LazyLogger.fromClazz(GitHubLibActivityChecker.class)

    final Config config

    final GitHubLibActivityCheckerConfig gitHubCheckerConfig

    final HttpClient httpClient

    @PackageScope
    @groovy.transform.NullCheck
    GitHubLibActivityChecker(final Config config, final HttpClient httpClient) {
        this.config = config
        gitHubCheckerConfig = config.getLibActivityCheckerConfig(GitHubLibActivityCheckerConfig.class)
        this.httpClient = httpClient
    }

    /**
     * Local GitHub mappings are preferred over global mappings. This is because if a repository has recently moved
     * the user may provide the new location via a local mapping without having to wait for a new plugin release
     * containing the updated location.
     *
     * If a local GitHub mapping is equal to a global one it will be reported redundant since there is no added value.
     */
    @Override
    CheckResultBundle check(final MavenId mavenId) {
        final ResultBuilder resultBuilder = new ResultBuilder(mavenId, gitHubCheckerConfig)
        completeResultBuilder(resultBuilder)
        return resultBuilder.build()
    }

    private void completeResultBuilder(final ResultBuilder resultBuilder) {

        try {

            final String libGitHubPath = getGitHubPath(resultBuilder)

            // we need to distinguish between null (no mapping) and empty (not hosted)
            if (null == libGitHubPath) {
                LOGGER.info { "Cannot check lib '${resultBuilder.mavenId}' on GitHub. No mapping available." }
                resultBuilder.libActivityCheckResultBuilder.apiQueryResult(ApiQueryResult.MISSING_GITHUB_MAPPING)
            } else {
                checkLatestGitHubCommitForPath(libGitHubPath, resultBuilder)
            }
        } catch (final HttpResponseNotOkException e) {
            addApiQueryResultForHttpNotOkResponseException(resultBuilder, e)
        } catch (final SocketTimeoutException e) {
            LOGGER.info({ "Timeout during GitHub check for '${resultBuilder.mavenId}'. Bytes transferred: ${e.bytesTransferred}." }, e)
            resultBuilder.libActivityCheckResultBuilder.apiQueryResult(ApiQueryResult.TIMEOUT)
        }
    }

    /**
     * Gets path on GitHub for lib from check result. This requires resolution of a GitHub mapping. If a local mapping is
     * found it is marked as used in the redundant local config check result.
     *
     * @return GitHub path for lib. {@code null} if no mapping registered.
     */
    private String getGitHubPath(final ResultBuilder resultBuilder) {

        final String libPathKey = resultBuilder.mavenId.toString()
        final String localGitHubPath = gitHubCheckerConfig.localGitHubMappings[libPathKey]
        // we pull props lazily => best case is that GitHub is not queried for any lib => no global mappings needed
        final String globalGitHubPath = gitHubCheckerConfig.globalGitHubMappings[libPathKey]
        // valid GitHub path might be empty which means that the lib is not hosted on GitHub
        final boolean isLocalGitHubPathPresent = null != localGitHubPath
        final String gitHubPath = isLocalGitHubPathPresent ? localGitHubPath : globalGitHubPath

        // check for presence of local mapping required because global mapping might not exist
        if (isLocalGitHubPathPresent && localGitHubPath != globalGitHubPath) {
            resultBuilder.redundancyBuilder.usedLocalGitHubMappingKey(libPathKey)
            resultBuilder.configCacheDataBuilder
                    .localGitHubMappingKey(libPathKey)
                    .localGitHubMappingValue(gitHubPath)
        }

        return gitHubPath
    }

    private void checkLatestGitHubCommitForPath(final String libGitHubPath, final ResultBuilder resultBuilder) {

        if (libGitHubPath) {

            LOGGER.info { "Check latest commit for lib '${resultBuilder.mavenId}' on GitHub." }

            final HttpUrl httpUrl = buildGitHubQueryHttpUrl(libGitHubPath)
            final Map<String, String> gitHubHeaders = buildGitHubQueryHeaders()
            LOGGER.debug { "Sending request: '${httpUrl}'." }

            final Object jsonResponse = httpClient.sendRequestAndParseJsonResponse(httpUrl, gitHubHeaders)
            updateCheckResultFromGitHubQueryResponse(resultBuilder, jsonResponse)
        } else {
            LOGGER.info { "Lib '${resultBuilder.mavenId}' not hosted on GitHub." }
            resultBuilder.libActivityCheckResultBuilder.apiQueryResult(ApiQueryResult.UNKNOWN_TO_GITHUB)
        }
    }

    /**
     * @return Http-URL to query only the latest commit in the activity timeframe via GitHub API.
     *
     * @see <a href="https://docs.github.com/de/rest/commits/commits">GitHub API - Commits</a>
     */
    private HttpUrl buildGitHubQueryHttpUrl(final String libGitHubPath) {

        // May be 2 parts. Part I: repo/owner. Part II: path/within/repo.
        final String[] mappedFragments = libGitHubPath.split('#')
        final HttpUrl.Builder httpUrlBuilder = new HttpUrl.Builder()
                .scheme(HttpClient.HTTPS_SCHEME)
                .host('api.github.com')
                .addPathSegment('repos')
                .addPathSegments(mappedFragments[0])
                .addPathSegment('commits')
        // we skip the since param to collect commits outside the timeframe => useful if timeframe is reconfigured in one of the next task runs
                .addEncodedQueryParameter('until', config.activityTimeframeConfig.timeframeIso8601.maximum)
                .addEncodedQueryParameter('per_page', '1')

        if (mappedFragments.length > 1) {
            httpUrlBuilder.addEncodedQueryParameter('path', mappedFragments[1])
        }

        return httpUrlBuilder.build()
    }

    private Map<String, String> buildGitHubQueryHeaders() {
        // API recommends this header
        final Map<String, String> gitHubQueryHeaders = ['accept': 'application/vnd.github+json']

        if (gitHubCheckerConfig.personalAccessToken) {
            gitHubQueryHeaders['Authorization'] = 'token ' + gitHubCheckerConfig.personalAccessToken
        }

        return gitHubQueryHeaders
    }

    /**
     * For details about (un)boxing in Groovy:
     *
     * @see <a href="http://docs.groovy-lang.org/latest/html/documentation/#_types">Groovy Types</a>
     * @see <a href="https://docs.groovy-lang.org/latest/html/documentation/core-differences-java.html#_primitives_and_wrappers">Groovy Primitives And Wrappers</a>
     */
    private void updateCheckResultFromGitHubQueryResponse(final ResultBuilder resultBuilder, final Object queryResponse) {

        final Long latestGitHubCommitTimestamp = extractLatestCommitTimestampFromGitHubQueryResponse(queryResponse)

        if (latestGitHubCommitTimestamp) {

            final Range<Long> activityTimeframe = config.activityTimeframeConfig.timeframe
            final long daysSinceLatestGitHubCommit = TimeConverter.rangeOfMillisToDays(latestGitHubCommitTimestamp, activityTimeframe.maximum)

            resultBuilder.queryResultCacheDataBuilder.latestCommitTimestamp(latestGitHubCommitTimestamp)

            if (activityTimeframe.contains(latestGitHubCommitTimestamp)) {
                LOGGER.info { "Found commit in timeframe (timestamp: ${latestGitHubCommitTimestamp}) for '${resultBuilder.mavenId}' on GitHub." }
                resultBuilder.libActivityCheckResultBuilder.detailedApiQueryResult(ApiQueryResult.GITHUB_COMMIT_IN_TIMEFRAME,
                        ApiQueryResultDetail.DAYS_SINCE_LATEST_GITHUB_COMMIT, daysSinceLatestGitHubCommit)
            } else {
                LOGGER.info { "Found commit outside timeframe (timestamp: ${latestGitHubCommitTimestamp}) for '${resultBuilder.mavenId}' on GitHub." }
                resultBuilder.libActivityCheckResultBuilder.detailedApiQueryResult(ApiQueryResult.NO_GITHUB_COMMIT_IN_TIMEFRAME,
                        ApiQueryResultDetail.DAYS_SINCE_LATEST_GITHUB_COMMIT, daysSinceLatestGitHubCommit)
            }
        } else {
            // rare case, e.g. a newly created repo without commits
            LOGGER.info { "No commits found for '${resultBuilder.mavenId}' on GitHub." }
            resultBuilder.libActivityCheckResultBuilder.apiQueryResult(ApiQueryResult.NO_GITHUB_COMMIT_IN_TIMEFRAME)
        }
    }

    /**
     * @return Timestamp in millis of latest GitHub commit. {@code null} if query response iterable empty, i.e. though the
     * lib is known there is no information on commits.
     */
    @Nullable
    private static Long extractLatestCommitTimestampFromGitHubQueryResponse(final Object queryResponse) {

        try {
            final Iterable<?> queryResponseCollection = (Iterable<?>) queryResponse
            // base object is supposed to be an iterable => an explicit emptiness check helps us recognize structural changes and disallows null
            if (queryResponseCollection.empty) {
                return null
            }

            final String commitZonedDateTime = queryResponseCollection[0].commit.committer.date
            // date property must be set
            Objects.requireNonNull(commitZonedDateTime)
            return TimeConverter.iso8601StringToEpochMilli(commitZonedDateTime)

        } catch (final Exception e) {
            // as with Sonatype walking the JSON properties or parsing the timestamp from the datetime string may cause an
            // exception => fix this with a new plugin release and maybe for a checker config option to allow users to parse the timestamp themselves
            // (...though the JSON response structure remained stable over the years)
            throw new IllegalArgumentException("Could not parse timestamp in JSON returned from GitHub API: ${queryResponse}", e)
        }
    }

    private static void addApiQueryResultForHttpNotOkResponseException(
            final ResultBuilder result,
            final HttpResponseNotOkException exception) {

        final int responseCode = exception.getStatusCode()

        switch (responseCode) {

            case 403:
                result.libActivityCheckResultBuilder.detailedApiQueryResult(ApiQueryResult.INVALID_RESPONSE, ApiQueryResultDetail.RESPONSE_CODE, responseCode)
                LOGGER.warn { "Received '${responseCode}' when checking lib '${result.mavenId}' on GitHub. One reason could be an exceeded rate limit. If this continues to happen consider using the plugin with a personal GitHub access token." }
                break

            case 404:
                // We assume the provided GitHub mapping is valid, i.e. the query result must be valid. In this case it's
                // not.
                result.libActivityCheckResultBuilder.apiQueryResult(ApiQueryResult.INVALID_GITHUB_MAPPING)
                LOGGER.warn { "Received '${responseCode}' when checking lib '${result.mavenId}'. The GitHub mapping appears to be invalid." }
                break

            default:
                result.libActivityCheckResultBuilder.detailedApiQueryResult(ApiQueryResult.INVALID_RESPONSE, ApiQueryResultDetail.RESPONSE_CODE, responseCode)
                LOGGER.warn { "Received invalid response '${responseCode}' when checking lib '${result.mavenId}' via URL: '${exception.url}'." }
        }
    }

    private static class ResultBuilder {

        final MavenId mavenId

        final LibActivityCheckResult.SingleApiBuilder libActivityCheckResultBuilder = LibActivityCheckResult.singleApiBuilder(Api.GITHUB)

        final GitHubLibActivityCheckerConfigCacheData.Builder configCacheDataBuilder = GitHubLibActivityCheckerConfigCacheData.builder()

        private GitHubQueryResultCacheData.Builder queryResultCacheDataBuilder

        final GitHubLibActivityCheckerConfigRedundancyCheckResult.Builder redundancyBuilder

        @groovy.transform.NullCheck
        private ResultBuilder(final MavenId mavenId, final GitHubLibActivityCheckerConfig config) {
            this.mavenId = mavenId
            libActivityCheckResultBuilder.mavenId(mavenId)
            redundancyBuilder = GitHubLibActivityCheckerConfigRedundancyCheckResult.builder(config)
        }

        private getQueryResultCacheDataBuilder() {

            if (!queryResultCacheDataBuilder) {
                queryResultCacheDataBuilder = GitHubQueryResultCacheData.builder()
            }
            return queryResultCacheDataBuilder
        }

        private CheckResultBundle build() {

            if (queryResultCacheDataBuilder) {
                libActivityCheckResultBuilder.apiQueryResultCacheData(queryResultCacheDataBuilder.build())
            }

            final LibActivityCheckResult libActivityCheckResult = libActivityCheckResultBuilder
                    .checkerConfigCacheData(configCacheDataBuilder.build())
                    .build()
            final ConfigRedundancyCheckResult configRedundancyCheckResult = ConfigRedundancyCheckResult.builder()
                    .gitHubCheckerConfigRedundancy(redundancyBuilder.build())
                    .build()

            return new CheckResultBundle(libActivityCheckResult, configRedundancyCheckResult)
        }
    }
}