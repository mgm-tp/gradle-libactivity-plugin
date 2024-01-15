package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility
import okhttp3.HttpUrl
import org.apache.commons.lang3.Range

import javax.annotation.Nullable

@PackageScope
@TupleConstructor(post = { NullCheck.ALL_PROPS.call(this) })
@VisibilityOptions(Visibility.PACKAGE_PRIVATE)
class SonatypeLibActivityChecker implements IApiLibActivityChecker {

    final Config config

    final HttpClient httpClient

    private static final LazyLogger LOGGER = LazyLogger.fromClazz(SonatypeLibActivityChecker.class)

    @Override
    CheckResultBundle check(final MavenId mavenId) {
        final ResultBuilder resultBuilder = new ResultBuilder(mavenId)
        completeCheckResultBuilder(resultBuilder)
        return resultBuilder.build()
    }

    private void completeCheckResultBuilder(final ResultBuilder resultBuilder) {

        try {

            LOGGER.info { "Check lib '${resultBuilder.mavenId}' via Sonatype API." }

            final HttpUrl httpUrl = buildSonatypeQueryHttpUrl(resultBuilder)
            LOGGER.debug { "Sending request: '${httpUrl}'." }

            final Object jsonResponse = httpClient.sendRequestAndParseJsonResponse(httpUrl)
            updateCheckResultFromSonatypeQueryResponse(resultBuilder, jsonResponse)
        } catch (final HttpResponseNotOkException e) {
            addApiQueryResultForHttpNotOkResponseException(resultBuilder, e)
        } catch (final SocketTimeoutException e) {
            LOGGER.info({ "Timeout on Sonatype check for '${resultBuilder.mavenId}'. Bytes transferred: ${e.bytesTransferred}." }, e)
            resultBuilder.libActivityCheckResultBuilder.apiQueryResult(ApiQueryResult.TIMEOUT)
        }
    }

    /**
     * @return HTTP-URL to query only the latest release in the activity timeframe via Sonatype API.
     *
     * @see <a href="https://central.sonatype.org/search/rest-api-guide/">Sonatype REST API</a>
     */
    private static HttpUrl buildSonatypeQueryHttpUrl(final ResultBuilder resultBuilder) {

        return new HttpUrl.Builder()
                .scheme(HttpClient.HTTPS_SCHEME)
                .host('search.maven.org')
                .addPathSegment('solrsearch')
                .addPathSegment('select')
                .addEncodedQueryParameter('q', "g:${resultBuilder.mavenId.groupId}+AND+a:${resultBuilder.mavenId.artifactId}")
        // core parameter is important for correct sorting of releases (e.g. commons-io:commons-io gets wrong latest release without this option)
                .addQueryParameter('core', 'gav')
        // core param will return a list of results => we only need the 1st element / latest release
                .addQueryParameter('rows', '1')
                .addQueryParameter('wt', 'json')
                .build()
    }

    /**
     * @param queryResponse Sonatype query response as JSON
     * @param resultBuilder The check result to update
     */
    private void updateCheckResultFromSonatypeQueryResponse(final ResultBuilder resultBuilder, final Object queryResponse) {

        final Long latestCentralReleaseTimestamp = extractLatestReleaseTimestampFromSonatypeQueryResponse(queryResponse)

        if (latestCentralReleaseTimestamp) {
            LOGGER.info { "Check latest Central release timestamp ${latestCentralReleaseTimestamp} for lib '${resultBuilder.mavenId}'." }
            checkLatestCentralReleaseTimestamp(resultBuilder, latestCentralReleaseTimestamp)
        } else {
            // (partially) empty response
            LOGGER.info { "Received empty respone. Assume lib '${resultBuilder.mavenId}' has no release on Central." }
            resultBuilder.libActivityCheckResultBuilder.apiQueryResult(ApiQueryResult.UNKNOWN_TO_CENTRAL)
        }
    }

    /**
     * @return Timestamp of latest Central release. {@code null} if "docs" iterable property inside response empty, i.e. there
     * is no information on the queried lib.
     */
    @Nullable
    private static Long extractLatestReleaseTimestampFromSonatypeQueryResponse(final Object queryResponse) {

        try {
            final Iterable<?> docs = (Iterable<?>) queryResponse.response.docs
            // docs is supposed to be be an iterable and may be empty if no Central release found; if not empty the timestamp is a mandatory property
            return docs.empty ? null : Objects.requireNonNull(docs[0].timestamp) as Long
        } catch (final Exception e) {
            // when we walk the JSON properties or do the cast this may trigger an exception which signals a change in the
            // JSON structure => time for a new plugin release and maybe for a checker config option to allow users to parse the timestamp themselves
            // (...though the JSON response structure remained stable over the years)
            throw new IllegalStateException("Could not parse timestamp in JSON returned from Sonatype: ${queryResponse}", e)
        }
    }

    private void checkLatestCentralReleaseTimestamp(final ResultBuilder resultBuilder, final Long latestCentralReleaseEpochMilli) {

        final Range<Long> activityTimeframe = config.activityTimeframeConfig.timeframe
        final long daysSinceLatestCentralRelease = TimeConverter.rangeOfMillisToDays(latestCentralReleaseEpochMilli, activityTimeframe.maximum)

        resultBuilder.queryResultCacheDataBuilder.latestCentralReleaseTimestamp(latestCentralReleaseEpochMilli)

        if (activityTimeframe.contains(latestCentralReleaseEpochMilli)) {
            LOGGER.info { "Latest release of lib '${resultBuilder.mavenId}' is OK." }
            resultBuilder.libActivityCheckResultBuilder.detailedApiQueryResult(ApiQueryResult.CENTRAL_RELEASE_IN_TIMEFRAME,
                    ApiQueryResultDetail.DAYS_SINCE_LATEST_CENTRAL_RELEASE, daysSinceLatestCentralRelease)
        } else {
            LOGGER.info { "Lib '${resultBuilder.mavenId}' has outdated release." }
            resultBuilder.libActivityCheckResultBuilder.detailedApiQueryResult(ApiQueryResult.NO_CENTRAL_RELEASE_IN_TIMEFRAME,
                    ApiQueryResultDetail.DAYS_SINCE_LATEST_CENTRAL_RELEASE, daysSinceLatestCentralRelease)
        }
    }

    private static void addApiQueryResultForHttpNotOkResponseException(
            final ResultBuilder resultBuilder,
            final HttpResponseNotOkException exception) {

        final int responseCode = exception.getStatusCode()

        switch (responseCode) {

            case 404:
                resultBuilder.libActivityCheckResultBuilder.apiQueryResult(ApiQueryResult.UNKNOWN_TO_CENTRAL)
                LOGGER.info { "Received ${responseCode} when checking lib ${resultBuilder.mavenId}. Assume it was not released on Central." }
                break

            default:
                // We may need to be careful with response codes. Some of them could signal a serious error that might
                // persist for the rest of the API queries.
                resultBuilder.libActivityCheckResultBuilder.detailedApiQueryResult(ApiQueryResult.INVALID_RESPONSE, ApiQueryResultDetail.RESPONSE_CODE, responseCode)
                LOGGER.warn { "Received invalid response '${responseCode}' when checking lib '${resultBuilder.mavenId}' via URL: '${exception.url}'." }
        }
    }

    private static class ResultBuilder {

        final MavenId mavenId

        final LibActivityCheckResult.SingleApiBuilder libActivityCheckResultBuilder = LibActivityCheckResult.singleApiBuilder(Api.SONATYPE)

        private SonatypeQueryResultCacheData.Builder queryResultCacheDataBuilder

        private ResultBuilder(final MavenId mavenId) {
            Objects.requireNonNull(mavenId)
            this.mavenId = mavenId
            libActivityCheckResultBuilder.mavenId(mavenId)
        }

        private SonatypeQueryResultCacheData.Builder getQueryResultCacheDataBuilder() {

            if (!queryResultCacheDataBuilder) {
                queryResultCacheDataBuilder = SonatypeQueryResultCacheData.builder()
            }
            return queryResultCacheDataBuilder
        }

        private CheckResultBundle build() {

            if (queryResultCacheDataBuilder) {
                libActivityCheckResultBuilder.apiQueryResultCacheData(queryResultCacheDataBuilder.build())
            }
            return new CheckResultBundle(libActivityCheckResultBuilder.build(), ConfigRedundancyCheckResult.EMPTY)
        }
    }
}