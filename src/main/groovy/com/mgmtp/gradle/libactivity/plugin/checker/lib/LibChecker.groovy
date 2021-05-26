package com.mgmtp.gradle.libactivity.plugin.checker.lib

import com.mgmtp.gradle.libactivity.plugin.checker.config.LocalConfigChecker
import com.mgmtp.gradle.libactivity.plugin.config.GlobalConfig
import com.mgmtp.gradle.libactivity.plugin.config.LocalConfig
import com.mgmtp.gradle.libactivity.plugin.data.lib.Lib
import com.mgmtp.gradle.libactivity.plugin.data.lib.LibCoordinates
import com.mgmtp.gradle.libactivity.plugin.data.lib.LibDetail
import com.mgmtp.gradle.libactivity.plugin.data.lib.LibTag
import com.mgmtp.gradle.libactivity.plugin.logging.LazyLogger

import com.mgmtp.gradle.libactivity.plugin.result.data.AbstractCheckResult
import com.mgmtp.gradle.libactivity.plugin.result.format.collector.FormattedCheckResultCollectorFactory
import com.mgmtp.gradle.libactivity.plugin.result.format.formatter.CheckResultFormatterFactory
import com.mgmtp.gradle.libactivity.plugin.result.data.lib.LibCheckResult
import com.mgmtp.gradle.libactivity.plugin.result.writer.CheckResultWriterFactory
import com.mgmtp.gradle.libactivity.plugin.util.NullCheck
import groovy.json.JsonSlurper
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

import java.time.LocalDate
import java.util.concurrent.ThreadLocalRandom

@TupleConstructor( post = { NullCheck.ALL_PROPS.call( this)})
@VisibilityOptions( Visibility.PRIVATE)
class LibChecker {
    
    final GlobalConfig globalConfig

    final LocalConfig localConfig

    final LocalConfigChecker localConfigChecker

    private static final LazyLogger LOGGER = LazyLogger.fromClazz( LibChecker.class)

    private static final Closure<String> GENERIC_REQUEST_LOG = { String url -> "Sending request: '${ url}'."}

    private static final Closure<String> GENERIC_INVALID_RESPONSE_LOG = { int responseCode, Lib lib, String url ->
        "Received invalid response '${ responseCode}' when checking lib '${ lib}' via URL: '${ url}'."}

    private static final Closure<String> GENERIC_ERROR_LOG = { String msg, Throwable cause -> "${ msg} error: '${ cause.getMessage( )}'."}

    static LibChecker fromConfigBundle( GlobalConfig globalConfig, LocalConfig localConfig) {
        return new LibChecker( globalConfig, localConfig, LocalConfigChecker.fromLocalConfig( localConfig))
    }

    void checkLibCoordinates( Collection<LibCoordinates> libCoordinates) {
        LOGGER.info( 'Starting lib check.')
        LOGGER.info{ "Checking if any of ${ libCoordinates.size( )} libs are meant to be xcluded."}
        List<Lib> libs = libCoordinates.collect{ LibCoordinates coordinates -> Lib.fromCoordinates( coordinates)}.findAll{ Lib lib -> isNotXcludedLib( lib)}
        LOGGER.info{ "${ libs.size( )} libs passed xclusion filter."}
        libs.each{ Lib lib -> tagLib( lib)}
        List<AbstractCheckResult> checkResults = collectNonEmptyCheckResults( LibCheckResult.fromTaggedLibs( libs), localConfigChecker.result)
        if( checkResults) {
            CheckResultWriterFactory.getWriter( localConfig).write( getWritableCheckResults( checkResults))
        }
        else {
            LOGGER.info( 'No check results.')
        }
    }

    /** xcludes are check prior to xcludePatterns. */
    private boolean isNotXcludedLib( Lib lib) {
        String groupArtifactTuple = lib.coordinates.groupId + ':' + lib.coordinates.artifactId
        Optional<String> matchedXcludePattern = Optional.empty( )
        Optional<String> matchedXcludeItem = localConfig.xcludes.stream( )
                .filter{ String xclude ->
                    LOGGER.debug( "Checking lib '{}' against Xclude '{}'", lib, xclude)
                    xclude == groupArtifactTuple}
                .findFirst( )
        matchedXcludeItem.ifPresent{ String xclude -> localConfigChecker.markXcludeAsUsed( xclude)}
        if( !matchedXcludeItem) {
            matchedXcludePattern = localConfig.xcludePatterns.stream( )
                    .filter{ String pattern ->
                        LOGGER.debug( "Checking lib '{}' against Xclude Pattern '{}'", lib, pattern)
                        groupArtifactTuple.matches( pattern)}
                    .findFirst( )
            matchedXcludePattern.ifPresent{ String pattern -> localConfigChecker.markXcludePatternAsUsed( pattern)}
        }
        return !matchedXcludeItem && !matchedXcludePattern
    }

    /** Tag every lib based on Sonatype response. If release outdated collect more tags. */
    private void tagLib( Lib lib) {
        tagLibViaSonatypeQuery( lib)
        if( LibTag.OUTDATED_RELEASE in lib.tags) {
            tagApparentlyInactiveLib( lib)
        }
    }

    private void tagLibViaSonatypeQuery( Lib lib) {
        LOGGER.info( "Checking lib '{}' via Sonatype API.", lib)
        String query =  "q=g:\"${ lib.coordinates.groupId}\"+AND+a:\"${ lib.coordinates.artifactId}\"&core=gav&rows=100&wt=json"
        String encodedUrl = encodeAsHttpsUrl( 'search.maven.org', '/solrsearch/select', query)
        LOGGER.debug{ GENERIC_REQUEST_LOG.call( encodedUrl)}
        try {
            Object jsonObject = getConnectionContentAsJsonObject( encodedUrl)
            List<String> versions = jsonObject.response.docs.v
            List<Long> timestamps = jsonObject.response.docs.timestamp
            Map<String, Long> sonatypeQueryResult = [versions, timestamps].transpose( )
                    .collect{ Object list -> ( List<?>)list}
                    .collectEntries{ List<?> list -> [( list[0] as String): list[1] as Long]}
            if( !sonatypeQueryResult) {
                LOGGER.info( "Lib '{}' is unknown.", lib)
                lib.tags.add( LibTag.UNKNOWN)
            }
            else {
                LOGGER.info( "Checking current version age and latest release age for lib '{}'.", lib)
                tagLibBasedOnCurrentVersionAge( lib, sonatypeQueryResult)
                tagLibBasedOnLatestReleaseAge( lib, timestamps.first( ))
            }
        } catch ( HttpStatusException e) {
            tagLibBasedOnInvalidHttpResponse( lib, e)
        } catch ( IOException e) {
            LOGGER.error{ GENERIC_ERROR_LOG.call('Sonatype API', e)}
            throw new IllegalStateException( e)
        }
    }

    private void tagLibBasedOnCurrentVersionAge( Lib lib, Map<String, Long> sonatypeQueryResult) {
        if ( sonatypeQueryResult.containsKey( lib.coordinates.version)) {
            LocalDate earliestPossibleVersionReleaseDate = globalConfig.startOfCheckDate.minusMonths( localConfig.maxAgeCurrentVersionInMonths)
            LocalDate libVersionReleaseDate = getLocalDateFromUnixMillis( sonatypeQueryResult[lib.coordinates.version])
            if ( libVersionReleaseDate.isBefore( earliestPossibleVersionReleaseDate)) {
                LOGGER.info( "Lib '{}' has outdated version.", lib)
                lib.tags.add( LibTag.OUTDATED_VERSION)
                lib.details.put( LibDetail.VERSION_AGE, getFormattedYearsSince( libVersionReleaseDate))
            }
        }
        else {
            LOGGER.info( "Lib '{}' has unknown version.", lib)
            lib.tags.add( LibTag.UNKNOWN_VERSION)
        }
    }

    private void tagLibBasedOnLatestReleaseAge( Lib lib, long latestReleaseTimestamp) {
        LocalDate latestReleaseDate = getLocalDateFromUnixMillis( latestReleaseTimestamp)
        LocalDate earliestPossibleLatestReleaseDate = globalConfig.startOfCheckDate.minusMonths( localConfig.maxAgeLatestReleaseInMonths)
        if ( latestReleaseDate.isBefore( earliestPossibleLatestReleaseDate)) {
            LOGGER.info( "Lib '{}' has outdated release.", lib)
            lib.tags.add( LibTag.OUTDATED_RELEASE)
            lib.details.put( LibDetail.LATEST_RELEASE_AGE, getFormattedYearsSince( latestReleaseDate))
        }
        else {
            LOGGER.info( "Latest release of lib '{}' is OK.", lib)
            lib.tags.addAll( LibTag.ACTIVE, LibTag.RELEASE_OK)
        }
    }

    /**
     * Local GitHub mappings are preferred over global mappings. This is because if a repository has recently moved
     * the user may provide the new location via a local mapping without having to wait for a new plugin release
     * containing the updated location.
     *
     * If a local GitHub mapping is equal to a global one it will be reported redundant since there is no added value.
     */
    private void tagApparentlyInactiveLib( Lib lib) {
        String gitHubMappingKey = lib.toGroupSlashArtifactString( )
        Optional<String> localMappedLibNameOpt = Optional.ofNullable( localConfig.localGitHubMappings[gitHubMappingKey])
        Optional<String> globalMappedLibNameOpt = Optional.ofNullable( globalConfig.gitHubMappings[gitHubMappingKey])
        Optional<String> mappedLibNameOpt = localMappedLibNameOpt ?: globalMappedLibNameOpt

        if( !mappedLibNameOpt) {
            LOGGER.info( "Cannot check lib '{}' on GitHub. No mapping available.", lib)
            lib.tags.addAll( LibTag.UNAVAILABLE_RESULT, LibTag.NO_GITHUB_MAPPING)
        }
        else {
            String mappedLibName = mappedLibNameOpt.get( )
            if ( !mappedLibName) {
                LOGGER.info( "Lib '{}' not hosted on GitHub.", lib)
                lib.tags.addAll( LibTag.INACTIVE, LibTag.NO_GITHUB_HOSTING)
                tagLibViaMvnRepositoryQuery( lib)
            }
            else {
                tagLibViaGitHubQuery( lib, mappedLibName)
            }
            if( localMappedLibNameOpt && localMappedLibNameOpt != globalMappedLibNameOpt) {
                localConfigChecker.markLocalGitHubMappingKeyAsUsed( gitHubMappingKey)
            }
        }
    }

    private void tagLibViaGitHubQuery( Lib lib, String mappedLibName) {
        LOGGER.info( "Checking commits for apparantly inactive lib '{}' on GitHub.", lib)
        String[] mappedFragments = mappedLibName.split(':')
        LocalDate earliestPossibleLatestReleaseDate = globalConfig.startOfCheckDate.minusMonths( localConfig.maxAgeLatestReleaseInMonths)
        String gitHubPath = mappedFragments.length > 1 ? ( "&path=${ mappedFragments[1]}") : ''
        String query = "since=${ earliestPossibleLatestReleaseDate}T00:00:00Z${ gitHubPath}"
        String encodedUrl = encodeAsHttpsUrl( 'api.github.com', "/repos/${ mappedFragments[0]}/commits", query)
        Map<String, String> gitHubAuthorizationHeader = localConfig.gitHubOauthToken ? ['Authorization': "token ${ localConfig.gitHubOauthToken}"] : [:]
        LOGGER.debug{ GENERIC_REQUEST_LOG.call( encodedUrl)}
        try {
            int numCommitsInTimeFrame = getConnectionContentAsJsonObject( encodedUrl, gitHubAuthorizationHeader).size( )
            if ( numCommitsInTimeFrame > 0) {
                LOGGER.info( "Found {} commits for '{}' on GitHub.", numCommitsInTimeFrame, lib)
                lib.tags.addAll( LibTag.ACTIVE, LibTag.AT_LEAST_1_COMMIT)
                lib.details.put( LibDetail.NUM_COMMITS, numCommitsInTimeFrame)
            }
            else {
                LOGGER.info( "No commits found for '{}' on GitHub.", lib)
                lib.tags.addAll( LibTag.INACTIVE, LibTag.NO_COMMITS)
                tagLibViaMvnRepositoryQuery( lib)
            }
        } catch ( HttpStatusException e) {
            tagLibBasedOnInvalidHttpResponse( lib, e)
        } catch ( IOException e) {
            LOGGER.error{ GENERIC_ERROR_LOG.call( 'GitHub API', e)}
            throw new IllegalStateException( e)
        }
    }

    private static void tagLibViaMvnRepositoryQuery( Lib lib) {
        LOGGER.info( "Checking move status for inactive '{}' via MvnRepository.", lib)
        String encodedUrl = encodeAsHttpsUrl( 'mvnrepository.com', "/artifact/${ lib.toGroupSlashArtifactString( )}", null)
        LOGGER.debug{ GENERIC_REQUEST_LOG.call( encodedUrl)}
        try {
            getNewLibAddressViaMvnRepository( encodedUrl).ifPresent { newAddress ->
                LOGGER.info( "Lib '{}' moved to new address: '{}'.", lib, newAddress)
                lib.tags.add( LibTag.MOVED)
                lib.details.put( LibDetail.NEW_ADDRESS, newAddress)
            }
        } catch( HttpStatusException e) {
            LOGGER.warn{ GENERIC_INVALID_RESPONSE_LOG.call( e.statusCode, lib, encodedUrl)}
        } catch ( Exception e) {
            LOGGER.warn{ "${ GENERIC_ERROR_LOG.call( 'MvnRepository', e)} URL: '${ encodedUrl}'."}
        }
    }

    private String getWritableCheckResults( Collection<AbstractCheckResult> checkResults) {
        return checkResults.stream( )
                .map { AbstractCheckResult result ->
                    LOGGER.info( "Formatting check result '{}'", result.name)
                    CheckResultFormatterFactory.getFormatter( localConfig.outputFormat, result.class).format( result)}
                .collect( FormattedCheckResultCollectorFactory.getCollector( localConfig.outputFormat))
    }

    private String getFormattedYearsSince( LocalDate localDate) {
        return String.format( Locale.US, '%.1f', localDate.until( globalConfig.startOfCheckDate).toTotalMonths( ) / 12.0)
    }

    /** No API call. Care must be taken not to get locked out. How would a normal user act? */
    private static Optional<String> getNewLibAddressViaMvnRepository( String url) {
        Thread.sleep( ThreadLocalRandom.current( ).nextInt( 1000, 4001))
        List<String> newLinks = Jsoup.connect( url).userAgent( 'Mozilla').get( ).getElementsContainingOwnText( 'artifact was moved')
                .collect{ Element element -> element.getElementsByTag( 'a')}.flatten( )
                .collect{ Object element -> (( Element)element).attr( 'href')}
                .findAll{ String newLink -> newLink}
        return ( newLinks ? Optional.of( "https://mvnrepository.com${ newLinks.last( )}") : Optional.empty( )) as Optional<String>
    }

    private static void tagLibBasedOnInvalidHttpResponse( Lib lib, HttpStatusException httpStatusException) {
        int responseCode = httpStatusException.getStatusCode( )
        String encodedUrl = httpStatusException.getUrl( )
        switch ( responseCode) {
            case 403:
                if ( encodedUrl.contains( 'api.github.com')) {
                    LOGGER.warn{ GENERIC_INVALID_RESPONSE_LOG.call( responseCode, lib, encodedUrl)}
                    lib.tags.addAll( LibTag.UNAVAILABLE_RESULT, LibTag.GITHUB_RESPONSE_403)
                    break
                }
                LOGGER.error{ GENERIC_INVALID_RESPONSE_LOG.call( responseCode, lib, encodedUrl)}
                throw new IllegalStateException( httpStatusException.getMessage( ))
            case 404:
                if( encodedUrl.contains( 'search.maven.org')) {
                    LOGGER.info{ GENERIC_INVALID_RESPONSE_LOG.call( responseCode, lib, encodedUrl)}
                    lib.tags.add( LibTag.UNKNOWN)
                }
                else {
                    LOGGER.warn{ GENERIC_INVALID_RESPONSE_LOG.call( responseCode, lib, encodedUrl)}
                    lib.tags.addAll( LibTag.UNAVAILABLE_RESULT, LibTag.GITHUB_RESPONSE_404)
                }
                break
            default:
                LOGGER.error{ GENERIC_INVALID_RESPONSE_LOG.call( responseCode, lib, encodedUrl)}
                throw new IllegalStateException( httpStatusException.getMessage( ))
        }
    }

    private static LocalDate getLocalDateFromUnixMillis( long unixMillis) {
        return LocalDate.ofEpochDay( Long.divideUnsigned( unixMillis, 86400000L))
    }

    private static String encodeAsHttpsUrl( String authority, String path, String query) {
        try {
            return new URI( 'https', authority, path, query, null).toASCIIString( )
        } catch ( URISyntaxException e) {
            LOGGER.error{ GENERIC_ERROR_LOG.call( 'URL construction', e)}
            throw new IllegalStateException( e)
        }
    }

    private static Object getConnectionContentAsJsonObject( String encodedUrl) {
        return getConnectionContentAsJsonObject( encodedUrl, [:])
    }

    private static Object getConnectionContentAsJsonObject( String encodedUrl, Map<String, String> requestHeaderParams) {
        HttpURLConnection connection = ( HttpURLConnection)new URL( encodedUrl).openConnection( )
        requestHeaderParams.forEach{ String key, String value -> connection.setRequestProperty( key, value)}
        connection.connect( )
        int responseCode = connection.responseCode
        if( responseCode != 200) {
            throw new HttpStatusException( "Request '${ encodedUrl}' received invalid response ${ responseCode}", responseCode, encodedUrl)
        }
        return (( InputStream)connection.getContent( )).withCloseable { InputStream inputStream -> new JsonSlurper( ).parse( inputStream)}
    }

    private static List<AbstractCheckResult> collectNonEmptyCheckResults( AbstractCheckResult... checkResults) {
        return checkResults.findAll { AbstractCheckResult result -> result.groups}
    }
}