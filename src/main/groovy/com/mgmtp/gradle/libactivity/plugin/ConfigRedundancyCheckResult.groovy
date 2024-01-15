package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.EqualsAndHashCode
import groovy.transform.PackageScope
import groovy.transform.builder.Builder

import javax.annotation.Nullable

@PackageScope
@EqualsAndHashCode
class ConfigRedundancyCheckResult implements IConfigRedundancyAware {

    /**
     * Empty result meaning nothing has been checked.
     */
    @PackageScope
    static final ConfigRedundancyCheckResult EMPTY = builder().build()

    @Nullable
    final XclusionConfigRedundancyCheckResult xclusionConfigRedundancy

    @Nullable
    final GitHubLibActivityCheckerConfigRedundancyCheckResult gitHubCheckerConfigRedundancy

    @Builder(builderClassName = 'Builder')
    private ConfigRedundancyCheckResult(
            @Nullable final XclusionConfigRedundancyCheckResult xclusionConfigRedundancy,
            @Nullable final GitHubLibActivityCheckerConfigRedundancyCheckResult gitHubCheckerConfigRedundancy) {
        this.xclusionConfigRedundancy = xclusionConfigRedundancy
        this.gitHubCheckerConfigRedundancy = gitHubCheckerConfigRedundancy
    }

    @Override
    boolean hasRedundancy() {
        return [xclusionConfigRedundancy, gitHubCheckerConfigRedundancy]
                .findAll()
                .any { final IConfigRedundancyAware checkResult -> checkResult.hasRedundancy() }
    }
}