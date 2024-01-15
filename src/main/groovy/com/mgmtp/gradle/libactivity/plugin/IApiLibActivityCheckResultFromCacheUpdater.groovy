package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope

@PackageScope
interface IApiLibActivityCheckResultFromCacheUpdater {

    CheckResultBundle updateCheckResultFromCache(LibActivityCheckResult checkResultFromCache)
}