package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

@PackageScope
@TupleConstructor
@VisibilityOptions(Visibility.PRIVATE)
class ConfigRedundancyCheckResultFormatterFactory {

    @PackageScope
    static IConfigRedundancyCheckResultFormatter getFormatter(final OutputFormat outputFormat) {

        switch (outputFormat) {

            case OutputFormat.TXT:
                return new PlainTextConfigRedundancyCheckResultFormatter()
            case OutputFormat.JSON:
                return new JsonConfigRedundancyCheckResultFormatter()
            default:
                throw new IllegalArgumentException("No ${IConfigRedundancyCheckResultFormatter.class.simpleName} registered for output format ${outputFormat}.")
        }
    }
}