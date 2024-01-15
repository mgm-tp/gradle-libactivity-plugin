package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

@PackageScope
@TupleConstructor
@VisibilityOptions(Visibility.PRIVATE)
final class ApiLibActivityCheckerConfigCacheDataValidatorFactory {

    @PackageScope
    static <T extends IApiLibActivityCheckerConfigCacheData> IApiLibActivityCheckerConfigCacheDataValidator<T> getValidator(
            final Class<T> checkerConfigCacheDataClazz, final Config config) {

        switch(checkerConfigCacheDataClazz) {

            case GitHubLibActivityCheckerConfigCacheData.class:
                return new GitHubLibActivityCheckerConfigCacheDataValidator(config)
            default:
                return new NopApiLibActivityCheckerConfigCacheDataValidator()
        }
    }
}