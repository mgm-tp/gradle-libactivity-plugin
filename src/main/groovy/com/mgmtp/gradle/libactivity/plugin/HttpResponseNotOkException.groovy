package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

/**
 * To escalate on API responses other than 200 / OK.
 */
@PackageScope
@TupleConstructor
@VisibilityOptions(Visibility.PACKAGE_PRIVATE)
@groovy.transform.NullCheck(includeGenerated = true)
class HttpResponseNotOkException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L

    final String url

    final int statusCode
}