package com.mgmtp.gradle.libactivity.plugin


import groovy.transform.PackageScope

@PackageScope
interface ILibActivityCheckResultFormatter {

    String format(Collection<LibActivityCheckResult> checkResults)
}