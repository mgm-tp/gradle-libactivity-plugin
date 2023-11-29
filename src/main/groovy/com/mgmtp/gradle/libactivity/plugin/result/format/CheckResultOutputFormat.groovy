package com.mgmtp.gradle.libactivity.plugin.result.format


import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

@TupleConstructor
@VisibilityOptions(Visibility.PRIVATE)
enum CheckResultOutputFormat {

    TXT,
    JSON

    static CheckResultOutputFormat parse(String format) {
        return Optional.ofNullable(values().find { CheckResultOutputFormat writableFormat -> writableFormat.name().equalsIgnoreCase(format) })
                .orElseThrow { new IllegalArgumentException("Invalid output format: ${format} ---> Valid options: ${values()}") }
    }
}