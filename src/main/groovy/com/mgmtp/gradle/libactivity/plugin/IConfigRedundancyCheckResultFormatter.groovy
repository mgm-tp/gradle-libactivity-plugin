package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope

@PackageScope
interface IConfigRedundancyCheckResultFormatter {

    /**
     * @return Formatted redundancy check result. Empty string if no content.
     */
    String format(ConfigRedundancyCheckResult checkResult)
}