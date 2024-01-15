package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

@PackageScope
@TupleConstructor(post = { NullCheck.ALL_PROPS.call(this) })
@VisibilityOptions(Visibility.PACKAGE_PRIVATE)
class LibActivityCheckResultCacheObject implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L

    final long saveTimestampEpochMilli

    final LibActivityCheckResult checkResult
}