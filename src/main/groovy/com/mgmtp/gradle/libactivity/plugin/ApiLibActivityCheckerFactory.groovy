package com.mgmtp.gradle.libactivity.plugin


import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

@PackageScope
@TupleConstructor
@VisibilityOptions(Visibility.PRIVATE)
final class ApiLibActivityCheckerFactory {

    @PackageScope
    static IApiLibActivityChecker getChecker(final Api api, final Config config) {

        switch (api) {
            case Api.SONATYPE:
                return new SonatypeLibActivityChecker(config, config.httpClient)
            case Api.GITHUB:
                return new GitHubLibActivityChecker(config, config.httpClient)
            default:
                throw new IllegalArgumentException("No lib activity checker for API ${api} registered.")
        }
    }
}