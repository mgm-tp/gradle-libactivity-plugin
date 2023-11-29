package com.mgmtp.gradle.libactivity.plugin.result.writer


import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

@TupleConstructor
@VisibilityOptions(Visibility.PRIVATE)
enum CheckResultOutputChannel {

    FILE,
    CONSOLE
}