package com.mgmtp.gradle.libactivity.plugin


import groovy.json.JsonBuilder
import groovy.transform.PackageScope

import javax.annotation.Nullable

@PackageScope
class JsonConfigRedundancyCheckResultFormatter implements IConfigRedundancyCheckResultFormatter {

    private static final LazyLogger LOGGER = LazyLogger.fromClazz(JsonConfigRedundancyCheckResultFormatter.class)

    @Override
    String format(final ConfigRedundancyCheckResult checkResult) {

        if (checkResult.hasRedundancy()) {

            return new JsonBuilder(
                    xclusionConfigRedundancy: getXclusionConfigRedundancy(checkResult.xclusionConfigRedundancy),
                    gitHubCheckerConfigRedundancy: getGitHubCheckerConfigRedundancy(checkResult.gitHubCheckerConfigRedundancy),
                    CheckResultJsonGenerator.INSTANCE
            ).toPrettyString()
        }

        LOGGER.info("Formatter( target: ${this.class}; format: JSON) received empty check result. Nothing to format.")
        return ''
    }

    private static Map<String, List<String>> getXclusionConfigRedundancy(@Nullable final XclusionConfigRedundancyCheckResult xclusionConfigRedundancy) {

        if (xclusionConfigRedundancy?.hasRedundancy()) {
            return [
                    unusedLibXcludes       : xclusionConfigRedundancy.unusedLibXcludes.sort(),
                    unusedLibXcludePatterns: xclusionConfigRedundancy.unusedLibXcludePatterns.sort()]
        }
        // JSON generator is configured to drop nulls
        return null
    }

    private static Map<String, List<String>> getGitHubCheckerConfigRedundancy(
            @Nullable final GitHubLibActivityCheckerConfigRedundancyCheckResult gitHubCheckerConfigRedundancy) {

        if (gitHubCheckerConfigRedundancy?.hasRedundancy()) {
            return [unusedGitHubMappingKeys: gitHubCheckerConfigRedundancy.unusedLocalGitHubMappingKeys.sort()]
        }

        return null
    }
}