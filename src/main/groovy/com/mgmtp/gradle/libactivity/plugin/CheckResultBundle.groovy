package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.EqualsAndHashCode
import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

@PackageScope
@EqualsAndHashCode
@TupleConstructor(post = { NullCheck.ALL_PROPS })
@VisibilityOptions(Visibility.PACKAGE_PRIVATE)
class CheckResultBundle {

    final LibActivityCheckResult libActivityCheckResult

    final ConfigRedundancyCheckResult configRedundancyCheckResult
}