package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope

@PackageScope
abstract class AbstractLibActivityCheckResultFormatter implements ILibActivityCheckResultFormatter {
    
    private static final LazyLogger LOGGER = LazyLogger.fromClazz(AbstractLibActivityCheckResultFormatter.class)

    private final CheckResultReportConfig reportConfig

    @groovy.transform.NullCheck
    protected AbstractLibActivityCheckResultFormatter(final CheckResultReportConfig reportConfig) {
        this.reportConfig = reportConfig
    }

    @Override
    String format(final Collection<LibActivityCheckResult> checkResults) {

        if (checkResults) {

            if (reportConfig.libActivityCheckResultGrouping) {
                final Map<Comparable<?>, ?> groupedCheckResults = new GroupingSorter(reportConfig.libActivityCheckResultGrouping).groupAndSort(checkResults)
                return formatGroupedCheckResults(groupedCheckResults)
            }
            return formatPlainCheckResults(checkResults.sort(false))
        } else {
            LOGGER.info("Formatter( ${this.class}) received empty check results. Nothing to format.")
            return EMPTY_STRING
        }
    }

    protected abstract String formatPlainCheckResults(final Collection<LibActivityCheckResult> checkResults)

    protected abstract String formatGroupedCheckResults(final Map<Comparable<?>, ?> groupedCheckResults)
}