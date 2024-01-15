package com.mgmtp.gradle.libactivity.plugin

import groovy.json.JsonBuilder
import groovy.transform.PackageScope

@PackageScope
class JsonLibActivityCheckResultFormatter extends AbstractLibActivityCheckResultFormatter {

    private final CheckResultReportConfig reportConfig

    @PackageScope
    JsonLibActivityCheckResultFormatter(final CheckResultReportConfig reportConfig) {
        super(reportConfig)
        this.reportConfig = reportConfig
    }

    @Override
    protected String formatPlainCheckResults(final Collection<LibActivityCheckResult> checkResults) {
        final List<Map<String, ?>> jsonContent = checkResults.collect { final LibActivityCheckResult checkResult -> formatCheckResult(checkResult) }
        return new JsonBuilder(jsonContent, CheckResultJsonGenerator.INSTANCE).toPrettyString()
    }

    @Override
    protected String formatGroupedCheckResults(final Map<Comparable<?>, ?> groupedCheckResults) {

        final Map<ILibActivityCheckResultGroupKeysProvider, Boolean> groupsWithSortOption = reportConfig.libActivityCheckResultGrouping
        final List<ILibActivityCheckResultGroupKeysProvider> groupKeysProviders = groupsWithSortOption.keySet().toList()
        final Map<String, ?> jsonContent = formatGroupedCheckResultsIndexAware(groupedCheckResults, groupKeysProviders, 0)

        return new JsonBuilder(jsonContent, CheckResultJsonGenerator.INSTANCE).toPrettyString()
    }

    private static Map<String, ?> formatGroupedCheckResultsIndexAware(
            final Map<Comparable<?>, ?> groupedCheckResults,
            final Collection<ILibActivityCheckResultGroupKeysProvider> groupKeysProviders,
            final int groupKeysProviderIndex) {

        final Map<String, ?> formattedGroupedCheckResults = [:]

        groupedCheckResults.each { final Comparable<?> groupKey, final Object group ->

            if (group instanceof Map<Comparable<?>, ?>) {
                formattedGroupedCheckResults[formatGroupKey(groupKey)] = formatGroupedCheckResults(group, groupKeysProviders, groupKeysProviderIndex + 1)
            } else if (group instanceof List<LibActivityCheckResult>) {
                formattedGroupedCheckResults[formatGroupKey(groupKey)] = formatCheckResultGroup(group)
            }
        }

        return [(formatGroupKeysProvider(groupKeysProviders[groupKeysProviderIndex])): formattedGroupedCheckResults]
    }

    private static String formatGroupKey(final Comparable<?> groupKey) {

        if (null == groupKey) {
            return 'NOT_APPLICABLE'
        }
        if (groupKey instanceof Enum<?>) {
            return EnumNameFormatter.formatAsKey(groupKey)
        }
        return groupKey as String
    }

    private static String formatGroupKeysProvider(final ILibActivityCheckResultGroupKeysProvider groupKeysProvider) {
        // at the moment we only have providers that are enums
        return groupKeysProvider instanceof Enum<?> ? EnumNameFormatter.formatAsKey(groupKeysProvider) : groupKeysProvider as String
    }

    private static List<Map<String, ?>> formatCheckResultGroup(final List<LibActivityCheckResult> checkResultGroup) {
        return checkResultGroup.collect { final LibActivityCheckResult checkResult -> formatCheckResult(checkResult) }
    }

    private static Map<String, ?> formatCheckResult(final LibActivityCheckResult checkResult) {

        return [
                mavenId                : formatMavenId(checkResult.mavenId),
                category               : EnumNameFormatter.formatAsKey(checkResult.category),
                detailedApiQueryResults: formatApiToDetailedApiQueryResults(checkResult.detailedApiQueryResults),
                fromCache              : checkResult.fromCache]
    }

    private static Map<String, String> formatMavenId(final MavenId mavenId) {
        return [
                groupId   : mavenId.groupId,
                artifactId: mavenId.artifactId]
    }

    private static Map<Api, ?> formatApiToDetailedApiQueryResults(final Map<Api, DetailedApiQueryResult> apiToDetailedQueryResult) {
        return apiToDetailedQueryResult.collectEntries { final Api api, final DetailedApiQueryResult detailedQueryResult ->
            [(EnumNameFormatter.formatAsKey(api)): formatDetailedApiQueryResult(detailedQueryResult)]
        }.sort()
    }

    private static Map<String, ?> formatDetailedApiQueryResult(final DetailedApiQueryResult detailedQueryResult) {
        return [
                queryResult: EnumNameFormatter.formatAsKey(detailedQueryResult.queryResult),
                details    : formatApiQueryResultDetails(detailedQueryResult.details)]
    }

    private static Map<String, String> formatApiQueryResultDetails(final Map<ApiQueryResultDetail, Comparable<?>> apiQueryResultDetails) {
        return apiQueryResultDetails.collectEntries { final ApiQueryResultDetail detailKey, final Comparable<?> detailValue ->
            [(EnumNameFormatter.formatAsKey(detailKey)): detailValue as String]
        }.sort()
    }
}