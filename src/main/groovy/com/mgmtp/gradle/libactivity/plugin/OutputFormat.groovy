package com.mgmtp.gradle.libactivity.plugin


import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

@TupleConstructor
@VisibilityOptions(Visibility.PRIVATE)
enum OutputFormat {

    TXT,
    JSON
}