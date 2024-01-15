package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope

@PackageScope
class GitHubLibActivityCheckResultFromCacheUpdater extends AbstractApiLibActivityCheckResultFromCacheUpdater {

    private final Config config

    @PackageScope
    GitHubLibActivityCheckResultFromCacheUpdater(final Config config) {
        super(config)
        this.config = config
    }

    @Override
    CheckResultBundle updateCheckResultFromCache(final LibActivityCheckResult checkResultFromCache) {

        final LibActivityCheckResult.Builder updatedCheckResultBuilder = checkResultFromCache.toBuilder()

        updateDaysSinceLatestActivity(ApiQueryResultDetail.DAYS_SINCE_LATEST_GITHUB_COMMIT, Api.GITHUB, checkResultFromCache, updatedCheckResultBuilder)

        // update used GitHub mapping keys if check result has a user-declared mapping
        final GitHubLibActivityCheckerConfigRedundancyCheckResult.Builder gitHubConfigRedundancyBuilder = GitHubLibActivityCheckerConfigRedundancyCheckResult
                .builder(config.getLibActivityCheckerConfig(GitHubLibActivityCheckerConfig.class))
        final IApiLibActivityCheckerConfigCacheData gitHubCheckerConfigCacheData = checkResultFromCache.cacheData.checkerConfigCacheData[Api.GITHUB]

        if (gitHubCheckerConfigCacheData && gitHubCheckerConfigCacheData instanceof GitHubLibActivityCheckerConfigCacheData) {

            final String gitHubMappingKey = ((GitHubLibActivityCheckerConfigCacheData) gitHubCheckerConfigCacheData).localGitHubMappingKey

            if (null != gitHubMappingKey) {
                gitHubConfigRedundancyBuilder.usedLocalGitHubMappingKey(gitHubMappingKey)
            }
        }

        final ConfigRedundancyCheckResult configRedundancyCheckResult = ConfigRedundancyCheckResult.builder()
                .gitHubCheckerConfigRedundancy(gitHubConfigRedundancyBuilder.build())
                .build()

        return new CheckResultBundle(updatedCheckResultBuilder.build(), configRedundancyCheckResult)
    }
}