package com.mgmtp.gradle.libactivity.plugin.result.format

import com.mgmtp.gradle.libactivity.plugin.result.format.collector.FormattedCheckResultCollector
import com.mgmtp.gradle.libactivity.plugin.result.format.collector.json.JsonFormattedCheckResultCollector
import com.mgmtp.gradle.libactivity.plugin.result.format.collector.plaintext.PlainTextFormattedCheckResultCollector
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

/** There is a default collector to aggregate formatted check results into a total representation. */
@TupleConstructor
@VisibilityOptions(Visibility.PRIVATE)
enum CheckResultOutputFormat {

    TXT(PlainTextFormattedCheckResultCollector.class),
    JSON(JsonFormattedCheckResultCollector.class)

    final Class<FormattedCheckResultCollector<?, ?>> formattedCheckResultCollectorClazz

    static CheckResultOutputFormat parse(String format) {
        return Optional.ofNullable(values().find { CheckResultOutputFormat writableFormat -> writableFormat.name().equalsIgnoreCase(format) })
                .orElseThrow { new IllegalArgumentException("Invalid output format: ${format} ---> Valid options: ${values()}") }
    }
}