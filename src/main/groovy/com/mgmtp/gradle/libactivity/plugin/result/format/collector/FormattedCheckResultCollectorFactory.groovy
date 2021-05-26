package com.mgmtp.gradle.libactivity.plugin.result.format.collector

import com.mgmtp.gradle.libactivity.plugin.result.format.CheckResultOutputFormat
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

@TupleConstructor
@VisibilityOptions( Visibility.PRIVATE)
class FormattedCheckResultCollectorFactory {

    static FormattedCheckResultCollector<?,?> getCollector( CheckResultOutputFormat outputFormat) {
        return outputFormat.formattedCheckResultCollectorClazz.getDeclaredConstructor( ).newInstance( )
    }
}