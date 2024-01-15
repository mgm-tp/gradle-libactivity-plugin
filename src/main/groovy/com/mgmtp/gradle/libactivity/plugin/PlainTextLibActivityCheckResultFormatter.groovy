package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope
import org.apache.commons.lang3.StringUtils

import java.time.Duration

@PackageScope
class PlainTextLibActivityCheckResultFormatter extends AbstractLibActivityCheckResultFormatter {

    private static final String CHECK_RESULT_SECTION_HEADLINE = 'Lib Activity Check Results'

    private static final String DOUBLE_LINEBREAK = System.lineSeparator() * 2

    private static final String EMPTY_STRING = ''

    private final Config config

    @PackageScope
    @groovy.transform.NullCheck
    PlainTextLibActivityCheckResultFormatter(final Config config) {
        super(config.checkResultReportConfig)
        this.config = config
    }

    @Override
    protected String formatPlainCheckResults(final Collection<LibActivityCheckResult> checkResults) {

        return formatSelectedConfigPropsSection() +
                DOUBLE_LINEBREAK +
                HeadlineFormatter.format(CHECK_RESULT_SECTION_HEADLINE) +
                checkResults
                        .collect { final LibActivityCheckResult checkResult -> formatPlainCheckResult(checkResult) }
                        .join(System.lineSeparator())

    }

    private static String formatPlainCheckResult(final LibActivityCheckResult checkResult) {

        final Map<String, String> checkResultProps = [:]
        checkResultProps['Group ID'] = checkResult.mavenId.groupId
        checkResultProps['Artifact ID'] = checkResult.mavenId.artifactId
        checkResultProps['Category'] = EnumNameFormatter.formatReadable(checkResult.category)

        checkResult.detailedApiQueryResults.each { final Api api, final DetailedApiQueryResult detailedApiQueryResult ->

            final String apiString = EnumNameFormatter.formatReadable(api)
            checkResultProps[apiString + ' query result'] = EnumNameFormatter.formatReadable(detailedApiQueryResult.queryResult)

            detailedApiQueryResult.details.each { final ApiQueryResultDetail detailKey, final Comparable<?> detailValue ->
                checkResultProps[apiString + ' Detail "' + EnumNameFormatter.formatReadable(detailKey) + '"'] = detailValue as String
            }
        }

        checkResultProps['From Cache'] = checkResult.fromCache.toString()

        return HeadlineFormatter.format(checkResult.mavenId.toString(), '-', 1) +
                new FormattedTableBuilder()
                        .column(EMPTY_STRING, checkResultProps.keySet().toList())
                        .column(EMPTY_STRING, checkResultProps.values().toList())
                        .invisibleBorder()
                        .skipColumnHeadline()
                        .build()
    }

    @Override
    protected String formatGroupedCheckResults(final Map<Comparable<?>, ?> groupedCheckResults) {

        final Map<ILibActivityCheckResultGroupKeysProvider, Boolean> groupsWithSortOption = config.checkResultReportConfig.libActivityCheckResultGrouping
        final List<ILibActivityCheckResultGroupKeysProvider> groupKeysProviders = groupsWithSortOption.keySet().toList()
        final Map<ILibActivityCheckResultGroupKeysProvider, List<Comparable<?>>> columns = prepareColumns(groupKeysProviders)
        fillColumns(columns, groupKeysProviders, 0, groupedCheckResults)

        return formatHeadlineAndTable(columns)
    }

    private static Map<ILibActivityCheckResultGroupKeysProvider, List<Comparable<?>>> prepareColumns(
            final List<ILibActivityCheckResultGroupKeysProvider> columnGroupKeys) {
        return columnGroupKeys.collectEntries { final ILibActivityCheckResultGroupKeysProvider columnGroupKey -> [(columnGroupKey): []] }
    }

    /**
     * @param columns Table column map to which keys (group keys providers) have been added in the order of table columns.
     * Fills columns prior to current column with {@code null} placeholders. This should be done after element insertion in the
     * current column if iterating over more than one group.
     */
    private static void padPriorColumns(
            final int currentGroupIndex,
            final List<ILibActivityCheckResultGroupKeysProvider> groupKeysProviders,
            final int groupKeysProviderIndex,
            final Map<ILibActivityCheckResultGroupKeysProvider, List<Comparable<?>>> columns) {

        if (currentGroupIndex > 0) {
            groupKeysProviders.subList(0, groupKeysProviderIndex).each { final ILibActivityCheckResultGroupKeysProvider priorGroupKeysProvider ->
                columns[priorGroupKeysProvider] << null
            }
        }
    }

    /**
     * @param columns Ordered columns to be filled. The keys should be the same as in the list of group keys providers.
     * @param groupKeysProviders List of group keys providers from which the current column / kind of check
     * result group type will be read.
     */
    private static void fillColumns(
            final Map<ILibActivityCheckResultGroupKeysProvider, List<Comparable<?>>> columns,
            final List<ILibActivityCheckResultGroupKeysProvider> groupKeysProviders,
            final int groupKeysProviderIndex,
            final Map<Comparable<?>, ?> groupedCheckResults) {

        final ILibActivityCheckResultGroupKeysProvider groupKeysProvider = groupKeysProviders[groupKeysProviderIndex]

        groupedCheckResults.eachWithIndex { final Comparable<?> groupKey, final Object groupValue, final int groupIndex ->

            columns[groupKeysProvider] << groupKey
            padPriorColumns(groupIndex, groupKeysProviders, groupKeysProviderIndex, columns)

            if (groupValue instanceof Map<Comparable<?>, ?>) {
                final Map<Comparable<?>, ?> nextGroups = (Map<Comparable<?>, ?>) groupValue
                fillColumns(columns, groupKeysProviders, groupKeysProviderIndex + 1, nextGroups)
            }
        }
    }

    private formatHeadlineAndTable(final Map<ILibActivityCheckResultGroupKeysProvider, List<Comparable<?>>> columns) {

        return formatSelectedConfigPropsSection() +
                DOUBLE_LINEBREAK +
                HeadlineFormatter.format(CHECK_RESULT_SECTION_HEADLINE) +
                formatGroupedCheckResultTable(columns)
    }

    private String formatSelectedConfigPropsSection() {

        final Map<String, String> selectedConfigProps = [
                'target project'                          : config.rootProjectName,
                'check timeframe'                         : config.activityTimeframeConfig.timeframeIso8601 as String,
                'maximum permitted age of latest activity': config.activityTimeframeConfig.maxDaysSinceLatestActivity + ' days',
                'cache lifespan of check result'          : formatCheckResultCacheLifespan(),
                'fail at check result categories'         : config.failAtLibActivityCheckResultCategories.sort().toString()
        ]

        return HeadlineFormatter.format('Selected Config Properties') +
                new FormattedTableBuilder()
                        .column(EMPTY_STRING, selectedConfigProps.keySet().toList())
                        .column(EMPTY_STRING, selectedConfigProps.values().toList())
                        .invisibleBorder()
                        .skipColumnHeadline()
                        .build()
    }

    private String formatCheckResultCacheLifespan() {
        return formatLargeIntegral(Duration.ofMillis(config.checkResultCacheConfig.lifetimeInMillis).toSeconds()) + ' s'
    }

    private static String formatGroupedCheckResultTable(final Map<ILibActivityCheckResultGroupKeysProvider, List<Comparable<?>>> columns) {

        final FormattedTableBuilder formattedTableBuilder = new FormattedTableBuilder()

        columns.each { final ILibActivityCheckResultGroupKeysProvider columnType, final List<Comparable<?>> columnElements ->
            formattedTableBuilder.column(formatColumnElement(columnType, columnType), formatColumnElements(columnType, columnElements))
        }

        return formattedTableBuilder.build()
    }

    private static List<String> formatColumnElements(final ILibActivityCheckResultGroupKeysProvider columnType, final List<Comparable<?>> columnElements) {
        final List<String> formattedColumnElements = []
        columnElements.each { final Object columnElement -> formattedColumnElements.add(formatColumnElement(columnType, columnElement)) }
        return formattedColumnElements
    }

    private static String formatColumnElement(final ILibActivityCheckResultGroupKeysProvider columnType, final Object columnElement) {

        if (null == columnElement) {
            return EMPTY_STRING
        }
        if (columnElement instanceof Enum<?>) {
            return EnumNameFormatter.formatReadable(columnElement)
        }
        if (columnElement instanceof Comparable<?>) {

            if (columnElement instanceof Integer || columnElement instanceof Long) {
                return formatLargeIntegral(columnElement)
            }
            if (columnElement instanceof Boolean) {

                if (columnElement) {
                    final int numLeftPadsForCross = columnType.toString().length() >> 1
                    return StringUtils.leftPad('x', numLeftPadsForCross)
                }
                return EMPTY_STRING
            }
        }
        return columnElement as String
    }

    /**
     * @param A large integer number, boxed.
     */
    private static String formatLargeIntegral(final Object largeInteger) {
        return String.format(Locale.US, '%,d', largeInteger)
    }
}