package com.mgmtp.gradle.libactivity.plugin

import org.gradle.api.Project

class CheckResultReportExtension {

    CheckResultReportExtension(final Project project) {
        dir = project.file(CheckLibActivityTask.DEFAULT_PROJECT_OUTPUT_DIR + '/report')
    }

    List<ILibActivityCheckResultGroupKeysProvider> groupLibActivityCheckResultsBy

    List<ILibActivityCheckResultGroupKeysProvider> getGroupLibActivityCheckResultsBy() {
        // we do not assign the default to the property because it depends on the format that may change during extension lifetime
        return null == groupLibActivityCheckResultsBy ? getDefaultGroupLibActivityCheckResultsBy() : groupLibActivityCheckResultsBy
    }

    private List<ILibActivityCheckResultGroupKeysProvider> getDefaultGroupLibActivityCheckResultsBy() {

        switch (format) {

            case OutputFormat.TXT:
                final List<ILibActivityCheckResultGroupKeysProvider> defaultGroupKeysProviders = []

                defaultGroupKeysProviders << BasicLibActivityCheckResultGroupKeysProvider.CATEGORY
                defaultGroupKeysProviders << BasicLibActivityCheckResultGroupKeysProvider.QUERY_RESULT_FROM_ACTIVE_CATEGORY
                defaultGroupKeysProviders << BasicLibActivityCheckResultGroupKeysProvider.MAVEN_ID
                defaultGroupKeysProviders << BasicLibActivityCheckResultGroupKeysProvider.API
                defaultGroupKeysProviders << BasicLibActivityCheckResultGroupKeysProvider.QUERY_RESULT_FROM_API

                defaultGroupKeysProviders.addAll(ApiQueryResultDetail.values())

                defaultGroupKeysProviders << BasicLibActivityCheckResultGroupKeysProvider.FROM_CACHE

                return defaultGroupKeysProviders
            case OutputFormat.JSON:
                return List.of()
            default:
                throw new IllegalStateException("No default groupLibActivityCheckResultsBy defined for format '${format}'")
        }
    }

    /**
     * List of providers for keys to group lib activity check results in the report. Set this empty to skip grouping.
     */
    // we reserve null for internal use to recognize when we need to apply a default => this is probably not what the user wants
    @groovy.transform.NullCheck
    void setGroupLibActivityCheckResultsBy(final List<ILibActivityCheckResultGroupKeysProvider> groupLibActivityCheckResultsBy) {
        this.groupLibActivityCheckResultsBy = groupLibActivityCheckResultsBy
    }

    /**
     * Collection of providers for group keys that should be sorted in reverse order in the report.
     */
    Collection<ILibActivityCheckResultGroupKeysProvider> sortLibActivityCheckResultGroupsReverse = Set.of()

    /**
     * Format to use for produced check result reports.
     */
    OutputFormat format = OutputFormat.TXT

    // because the grouping default depends on the format we cannot have the user set this null inadvertently
    @groovy.transform.NullCheck
    void setFormat(final OutputFormat format) {
        this.format = format
    }

    /**
     * Channels to write check result reports.
     */
    Collection<OutputChannel> channels = Set.of(OutputChannel.FILE)

    /**
     * Directory where the check result reports are written. Only relevant if writing to file.
     */
    File dir

    /**
     * File name of report that contains lib activity check results. Only relevant if writing to file.
     */
    String libActivityCheckResultFileName = 'libActivityReport'

    /**
     * File name of report that contains redundant entries of the plugin config in the target project. Only relevant if
     * writing to file.
     */
    String configRedundancyCheckResultFileName = 'configRedundancyReport'
}