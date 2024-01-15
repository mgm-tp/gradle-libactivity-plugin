package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

@PackageScope
@TupleConstructor
@VisibilityOptions(Visibility.PACKAGE_PRIVATE)
class NopApiLibActivityCheckerConfigCacheDataValidator implements IApiLibActivityCheckerConfigCacheDataValidator<IApiLibActivityCheckerConfigCacheData> {

    @Override
    boolean isCheckerConfigCacheDataValid(final IApiLibActivityCheckerConfigCacheData checkerConfigCacheData) {
        return true
    }
}