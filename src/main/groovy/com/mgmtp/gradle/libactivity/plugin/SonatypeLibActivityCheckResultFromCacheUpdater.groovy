package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope

@PackageScope
class SonatypeLibActivityCheckResultFromCacheUpdater extends AbstractApiLibActivityCheckResultFromCacheUpdater {

    @PackageScope
    SonatypeLibActivityCheckResultFromCacheUpdater(final Config config) {
        super(config)
    }

    @Override
    CheckResultBundle updateCheckResultFromCache(final LibActivityCheckResult checkResultFromCache) {

        final LibActivityCheckResult.Builder updatedCheckResultBuilder = checkResultFromCache.toBuilder()

        updateDaysSinceLatestActivity(
                ApiQueryResultDetail.DAYS_SINCE_LATEST_CENTRAL_RELEASE, Api.SONATYPE, checkResultFromCache, updatedCheckResultBuilder)

        return new CheckResultBundle(updatedCheckResultBuilder.build(), ConfigRedundancyCheckResult.EMPTY)
    }
}