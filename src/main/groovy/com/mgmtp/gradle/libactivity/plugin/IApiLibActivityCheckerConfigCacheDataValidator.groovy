package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope

@PackageScope
interface IApiLibActivityCheckerConfigCacheDataValidator<T extends IApiLibActivityCheckerConfigCacheData> {

    boolean isCheckerConfigCacheDataValid(T checkerConfigCacheData)
}