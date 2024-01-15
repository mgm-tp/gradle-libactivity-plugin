package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

@PackageScope
@TupleConstructor
@VisibilityOptions(Visibility.PRIVATE)
final class ApiLibActivityCheckResultUpdaterFactory {

    @PackageScope
    static IApiLibActivityCheckResultFromCacheUpdater getUpdater(final Api api, final Config config) {

        switch (api) {

            case Api.SONATYPE:
                return new SonatypeLibActivityCheckResultFromCacheUpdater(config)
            case Api.GITHUB:
                return new GitHubLibActivityCheckResultFromCacheUpdater(config)
            default:
                return new NopLibActivityCheckResultFromCacheUpdater()
        }
    }
}