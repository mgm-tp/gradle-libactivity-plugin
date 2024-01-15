package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope

@PackageScope
interface IApiLibActivityChecker {

    CheckResultBundle check(MavenId mavenId)
}