package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope

import javax.annotation.Nullable

@PackageScope
class PlainTextConfigRedundancyCheckResultFormatter implements IConfigRedundancyCheckResultFormatter {

    private static final LazyLogger LOGGER = LazyLogger.fromClazz(PlainTextConfigRedundancyCheckResultFormatter.class)

    @Override
    String format(final ConfigRedundancyCheckResult checkResult) {

        if (checkResult.hasRedundancy()) {

            final String redundancySectionsAsString = [
                    'Xclusion Config Redundancy'      : getXclusionConfigRedundancySection(checkResult.xclusionConfigRedundancy),
                    'GitHub Checker Config Redundancy': getGitHubCheckerConfigRedundancySection(checkResult.gitHubCheckerConfigRedundancy)
            ]
            // format section only if elements present
                    .findAll { final String sectionHeadline, final Map<String, Set<String>> sectionContent -> sectionContent }
                    .collect { final String sectionHeadline, final Map<String, Set<String>> redundancyTableHeadlineToElements ->
                        HeadlineFormatter.format(sectionHeadline, '=', 2) + formatRedundancyTable(redundancyTableHeadlineToElements)
                    }
            // redundancy sections have 2 lines of space between them
                    .join(System.lineSeparator() * 3)

            return HeadlineFormatter.format('Config Redundancy Check Result', 3) + redundancySectionsAsString
        }

        LOGGER.info("Formatter( target: ${this.class}; format: plain text) received empty check result. Nothing to format.")
        return ''
    }

    private static String formatRedundancyTable(final Map<String, Set<String>> redundancyTableHeadlineToElements) {

        return redundancyTableHeadlineToElements.collect { final String headline, final Set<String> elements ->
            HeadlineFormatter.format(headline, '-', 1) +
                    // after the table headline we insert sorted elements, one on each line
                    elements.sort().join(System.lineSeparator())
            // redundancy tables have 2 lines of space between them
        }.join(System.lineSeparator() * 2)
    }

    private static Map<String, Set<String>> getXclusionConfigRedundancySection(
            @Nullable final XclusionConfigRedundancyCheckResult xclusionConfigRedundancy) {

        if (xclusionConfigRedundancy?.hasRedundancy()) {
            return [
                    'Unused Lib Xcludes'        : xclusionConfigRedundancy.unusedLibXcludes,
                    'Unused Lib Xclude Patterns': xclusionConfigRedundancy.unusedLibXcludePatterns]
        }
        return [:]
    }

    private static Map<String, Set<String>> getGitHubCheckerConfigRedundancySection(
            @Nullable final GitHubLibActivityCheckerConfigRedundancyCheckResult gitHubCheckerConfigRedundancy) {

        if (gitHubCheckerConfigRedundancy?.hasRedundancy()) {
            return ['Unused Local GitHub Mapping Keys': gitHubCheckerConfigRedundancy.unusedLocalGitHubMappingKeys]
        }
        return [:]
    }
}