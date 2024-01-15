package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope

import javax.annotation.Nullable

@PackageScope
interface IApiQueryResultCacheData {

    @Nullable
    Long getLatestActivityTimestamp()
}