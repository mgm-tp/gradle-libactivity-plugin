package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

@PackageScope
@TupleConstructor
@VisibilityOptions(Visibility.PRIVATE)
class LibActivityCheckResultFormatterFactory {

    @PackageScope
    static ILibActivityCheckResultFormatter getFormatter(final Config config) {

        switch (config.checkResultReportConfig.format) {

            case OutputFormat.TXT:
                return new PlainTextLibActivityCheckResultFormatter(config)
            case OutputFormat.JSON:
                return new JsonLibActivityCheckResultFormatter(config.checkResultReportConfig)
            default:
                throw new IllegalArgumentException("No ${ILibActivityCheckResultFormatter.class.simpleName} registered for output format ${config.checkResultReportConfig.format}.")
        }
    }
}