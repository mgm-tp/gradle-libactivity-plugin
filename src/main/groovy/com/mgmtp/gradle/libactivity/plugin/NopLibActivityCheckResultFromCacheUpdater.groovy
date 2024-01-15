package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

/**
 * Returns a check result bundle with no update, i.e. with the provided lib activity check result and an empty config
 * usage result.
 */
@PackageScope
@TupleConstructor
@VisibilityOptions(Visibility.PACKAGE_PRIVATE)
class NopLibActivityCheckResultFromCacheUpdater implements IApiLibActivityCheckResultFromCacheUpdater {

    @Override
    CheckResultBundle updateCheckResultFromCache(final LibActivityCheckResult checkResultFromCache) {
        return new CheckResultBundle(checkResultFromCache, ConfigRedundancyCheckResult.EMPTY)
    }
}