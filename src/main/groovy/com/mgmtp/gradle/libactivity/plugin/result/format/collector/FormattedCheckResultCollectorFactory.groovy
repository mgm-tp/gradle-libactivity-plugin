package com.mgmtp.gradle.libactivity.plugin.result.format.collector

import com.mgmtp.gradle.libactivity.plugin.result.format.CheckResultOutputFormat
import com.mgmtp.gradle.libactivity.plugin.result.format.collector.json.JsonFormattedCheckResultCollector
import com.mgmtp.gradle.libactivity.plugin.result.format.collector.plaintext.PlainTextFormattedCheckResultCollector
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

@TupleConstructor
@VisibilityOptions(Visibility.PRIVATE)
class FormattedCheckResultCollectorFactory {

    static FormattedCheckResultCollector<?, ?> getCollector(CheckResultOutputFormat outputFormat) {

        switch (outputFormat) {
            
            case CheckResultOutputFormat.TXT:
                return new PlainTextFormattedCheckResultCollector()
            case CheckResultOutputFormat.JSON:
                return new JsonFormattedCheckResultCollector()
            default:
                throw new IllegalArgumentException("No formatter registered for output format '${outputFormat}'")
        }
    }
}