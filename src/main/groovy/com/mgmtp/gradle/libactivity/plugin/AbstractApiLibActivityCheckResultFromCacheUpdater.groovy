package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope
import org.apache.commons.lang3.Range

@PackageScope
abstract class AbstractApiLibActivityCheckResultFromCacheUpdater implements IApiLibActivityCheckResultFromCacheUpdater {

    private final Config config

    @groovy.transform.NullCheck
    protected AbstractApiLibActivityCheckResultFromCacheUpdater(final Config config) {
        this.config = config
    }

    protected void updateDaysSinceLatestActivity(
            final ApiQueryResultDetail detailKey,
            final Api api,
            final LibActivityCheckResult checkResult,
            final LibActivityCheckResult.Builder updatedCheckResultBuilder) {

        final Map<Api, DetailedApiQueryResult> detailedApiQueryResults = new HashMap<>(checkResult.detailedApiQueryResults)
        final DetailedApiQueryResult detailedApiQueryResult = detailedApiQueryResults[api]
        final Long latestActivityTimestamp = checkResult.cacheData.queryResultCacheData[api]?.latestActivityTimestamp

        if (latestActivityTimestamp) {

            final Range<Long> activityTimeframe = config.activityTimeframeConfig.timeframe
            final long updatedDaysSinceLatestActivity = TimeConverter.rangeOfMillisToDays(latestActivityTimestamp, activityTimeframe.maximum)
            final DetailedApiQueryResult updatedDetailedApiQueryResult = DetailedApiQueryResult.builder()
                    .apiQueryResult(detailedApiQueryResult.queryResult)
                    .apiQueryResultDetail(detailKey, updatedDaysSinceLatestActivity)
                    .build()
            detailedApiQueryResults[api] = updatedDetailedApiQueryResult
            updatedCheckResultBuilder.detailedApiQueryResults(detailedApiQueryResults)
        }
    }
}