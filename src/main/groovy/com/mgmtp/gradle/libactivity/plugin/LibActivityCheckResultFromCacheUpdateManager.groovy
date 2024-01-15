package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

import javax.annotation.Nullable

import static com.mgmtp.gradle.libactivity.plugin.LibActivityCheckResult.Category.*

@PackageScope
@TupleConstructor(post = { NullCheck.ALL_PROPS.call(this) })
@VisibilityOptions(Visibility.PRIVATE)
class LibActivityCheckResultFromCacheUpdateManager {

    final Config config

    /**
     * @return A new updater that applies updates based on the provided config.
     */
    @PackageScope
    static LibActivityCheckResultFromCacheUpdateManager fromConfig(final Config config) {
        return new LibActivityCheckResultFromCacheUpdateManager(config)
    }

    /**
     * @return The updated lib activity check result bundled with its checker config usage.
     */
    @PackageScope
    @Nullable
    CheckResultBundle updateCheckResultFromCache(final LibActivityCheckResult checkResultFromCache) {

        // checker config consistency check
        if (isCheckerConfigCacheDataValid(checkResultFromCache)) {

            final Map<Api, DetailedApiQueryResult> relevantApiToDetailedQueryResults = checkResultFromCache.detailedApiQueryResults
                    .findAll { final Api api, final DetailedApiQueryResult detailedQueryResult -> api in config.apis }

            if (relevantApiToDetailedQueryResults) {

                final LibActivityCheckResult.Category mostRelevantCategory = relevantApiToDetailedQueryResults.values()
                        .collect { final DetailedApiQueryResult detailedApiQueryResult -> detailedApiQueryResult.queryResult.category }
                        .min()

                if (RECHECK_NEEDED != mostRelevantCategory) {
                    // optimization idea: take shortcut to recognize inactive libs if no cached query result is missing for any
                    // API from current config AND if lower end of current timeframe >= lower end of former timeframe. Since we
                    // have only 2 APIs available at this moment it would not make sense to add complexity and to cache the
                    // timeframe minimum as well.
                    if (UNKNOWN == mostRelevantCategory) {
                        return updateUnknownLibActivityCheckResult(checkResultFromCache, relevantApiToDetailedQueryResults)
                    }
                    // lib is ACTIVE or INACTIVE (but may also contain UNKNOWN or RECHECK_NEEDED query results)
                    return updateActiveOrInactiveLibActivityCheckResult(checkResultFromCache, relevantApiToDetailedQueryResults)
                }
                // based on the query results that are compatible with the current configuration the most relevant category
                // tells us to do a recheck
            }
        }

        return null
    }

    private CheckResultBundle updateUnknownLibActivityCheckResult(
            final LibActivityCheckResult checkResultFromCache, final Map<Api, DetailedApiQueryResult> relevantApiToDetailedQueryResults) {

        final boolean cachedApiMissing = config.apis.any { final Api currentApi -> !relevantApiToDetailedQueryResults.containsKey(currentApi) }

        if (cachedApiMissing) {
            // there is an API in the current configuration for which we do not have a cached query result => user added
            // API that was not present in a former task run
            return null
        }

        return buildCheckResultBundle(checkResultFromCache, relevantApiToDetailedQueryResults)
    }

    @Nullable
    private CheckResultBundle updateActiveOrInactiveLibActivityCheckResult(
            final LibActivityCheckResult checkResultFromCache,
            final Map<Api, DetailedApiQueryResult> relevantApiToDetailedQueryResults) {

        for (final Api currentApi : config.apis) {

            if (!relevantApiToDetailedQueryResults[currentApi]) {
                // there is an API in the current configuration for which we do not have a cached query result => user added
                // API that was not present in a former task run OR it is an API from the former task run that was not
                // queried because the activity check ended early (i.e. a prior API reported the lib ACTIVE)
                return null
            }

            final IApiQueryResultCacheData apiQueryResultCacheData = checkResultFromCache.cacheData.queryResultCacheData[currentApi]

            if (apiQueryResultCacheData) {

                boolean isActive = config.activityTimeframeConfig.timeframe.contains(apiQueryResultCacheData.latestActivityTimestamp)

                if (isActive) {
                    return updateActiveLibActivityCheckResult(checkResultFromCache, relevantApiToDetailedQueryResults, currentApi)
                }
            }
        }
        // lib activity check result cannot be reused with ACTIVE category => now try to declare a former ACTIVE as INACTIVE
        // or to just reuse a former INACTIVE
        return updateInactiveLibActivityCheckResult(checkResultFromCache, relevantApiToDetailedQueryResults)
    }

    private CheckResultBundle updateActiveLibActivityCheckResult(
            final LibActivityCheckResult checkResultFromCache,
            final Map<Api, DetailedApiQueryResult> relevantApiToDetailedQueryResults,
            final Api api) {

        final DetailedApiQueryResult cachedDetailedApiQueryResult = relevantApiToDetailedQueryResults[api]
        // if the cached query result belongs to the INACTIVE category we have to replace it by its ACTIVE counterpart
        if (INACTIVE == cachedDetailedApiQueryResult.queryResult.category) {
            relevantApiToDetailedQueryResults[api] = cachedDetailedApiQueryResult.toBuilder()
            // by design we should have exactly 1 query result for each API that has category ACTIVE
                    .apiQueryResult(getFirstMatchingApiQueryResult(api, ACTIVE))
                    .build()
        }

        return buildCheckResultBundle(checkResultFromCache, relevantApiToDetailedQueryResults)
    }

    private CheckResultBundle updateInactiveLibActivityCheckResult(
            final LibActivityCheckResult checkResultFromCache,
            final Map<Api, DetailedApiQueryResult> relevantApiToDetailedQueryResults) {

        final boolean isRecheckNeededPresent = relevantApiToDetailedQueryResults.values()
                .any { final DetailedApiQueryResult detailedApiQueryResult -> RECHECK_NEEDED == detailedApiQueryResult.queryResult.category }

        if (isRecheckNeededPresent) {
            // Though we checked for RECHECK_NEEDED as most relevant category it may also be there as "not most relevant"
            // which can only happen if its query result co-occurs with one that belongs to the ACTIVE category.
            // Since a former ACTIVE lib would now turn INACTIVE it turns out that RECHECK_NEEDED becomes most relevant
            return null
        }

        final Map.Entry<Api, DetailedApiQueryResult> cachedActiveReportingApiToDetailedQueryResult = relevantApiToDetailedQueryResults
                .find { final Api api, final DetailedApiQueryResult detailedApiQueryResult -> ACTIVE == detailedApiQueryResult.queryResult.category }
        // if the cached query result belongs to the ACTIVE category we have to replace it by its INACTIVE counterpart
        if (cachedActiveReportingApiToDetailedQueryResult) {

            final Api cachedApi = cachedActiveReportingApiToDetailedQueryResult.key
            final DetailedApiQueryResult cachedDetailedApiQueryResult = cachedActiveReportingApiToDetailedQueryResult.value
            relevantApiToDetailedQueryResults[cachedApi] = cachedDetailedApiQueryResult.toBuilder()
            // by design we should have exactly 1 query result for each API that has category INACTIVE
                    .apiQueryResult(getFirstMatchingApiQueryResult(cachedApi, INACTIVE))
                    .build()
        }

        return buildCheckResultBundle(checkResultFromCache, relevantApiToDetailedQueryResults)
    }

    @Nullable
    private static ApiQueryResult getFirstMatchingApiQueryResult(final Api api, final LibActivityCheckResult.Category category) {
        return ApiQueryResult.values().find { final ApiQueryResult apiQueryResult -> apiQueryResult.apis.contains(api) && category == apiQueryResult.category }
    }

    private CheckResultBundle buildCheckResultBundle(
            final LibActivityCheckResult cachedCheckResult, final Map<Api, DetailedApiQueryResult> relevantApiToDetailedQueryResultsForCurrentApis) {
        // we need a new builder because building on top of the cached check result would keep query results from APIs that are no longer relevant
        final LibActivityCheckResult checkResultWithUpdatedQueryResults = LibActivityCheckResult.builder()
                .mavenId(cachedCheckResult.mavenId)
                .detailedApiQueryResults(relevantApiToDetailedQueryResultsForCurrentApis)
        // we mark the check result as coming "from cache" but do not transfer cache data since this is not needed outside the cache domain
                .fromCache(true)
                .build()

        return getCheckResultsUpdatedByApi(checkResultWithUpdatedQueryResults)
    }

    private CheckResultBundle getCheckResultsUpdatedByApi(final LibActivityCheckResult checkResult) {
        final List<CheckResultBundle> checkResultBundlesUpdatedByApi = checkResult.detailedApiQueryResults.keySet()
                .collect { final Api api -> ApiLibActivityCheckResultUpdaterFactory.getUpdater(api, config).updateCheckResultFromCache(checkResult) }
        return CheckResultMerger.mergeCheckResultBundles(checkResultBundlesUpdatedByApi)
    }

    private boolean isCheckerConfigCacheDataValid(final LibActivityCheckResult checkResultFromCache) {

        return checkResultFromCache.cacheData.checkerConfigCacheData
        // only cache data for currently configured APIs must be checked
                .findAll { final Api api, final IApiLibActivityCheckerConfigCacheData checkerConfigCacheData -> api in config.apis }
                .values()
        // returns true if empty which is OK for us
                .every { final IApiLibActivityCheckerConfigCacheData checkerConfigCacheData ->
                    ApiLibActivityCheckerConfigCacheDataValidatorFactory.getValidator(checkerConfigCacheData.class, config)
                            .isCheckerConfigCacheDataValid(checkerConfigCacheData)
                }
    }
}