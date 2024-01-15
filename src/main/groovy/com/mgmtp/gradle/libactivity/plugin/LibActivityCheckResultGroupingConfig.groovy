package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

@TupleConstructor(post = { NullCheck.ALL_PROPS.call(this) })
@VisibilityOptions(Visibility.PRIVATE)
class LibActivityCheckResultGroupingConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L


}