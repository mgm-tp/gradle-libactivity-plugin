package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.EqualsAndHashCode
import groovy.transform.PackageScope

@PackageScope
@EqualsAndHashCode
@groovy.transform.NullCheck
class GitHubLibActivityCheckerConfigRedundancyCheckResult implements IConfigRedundancyAware {

    final Set<String> unusedLocalGitHubMappingKeys

    @PackageScope
    GitHubLibActivityCheckerConfigRedundancyCheckResult(final Collection<String> unusedLocalGitHubMappingKeys) {
        this.unusedLocalGitHubMappingKeys = unusedLocalGitHubMappingKeys.asImmutable()
    }

    @PackageScope
    static Builder builder(final GitHubLibActivityCheckerConfig gitHubLibActivityCheckerConfig) {
        return new Builder(gitHubLibActivityCheckerConfig)
    }

    @Override
    boolean hasRedundancy() {
        return unusedLocalGitHubMappingKeys
    }

    @PackageScope
    static class Builder {

        final Set<String> unusedLocalGitHubMappingKeys

        private Builder(final GitHubLibActivityCheckerConfig gitHubLibActivityCheckerConfig) {
            unusedLocalGitHubMappingKeys = gitHubLibActivityCheckerConfig.localGitHubMappings.keySet()
        }

        @PackageScope
        Builder usedLocalGitHubMappingKey(final String usedLocalGitHubMappingKey) {
            this.unusedLocalGitHubMappingKeys.remove(usedLocalGitHubMappingKey)
            return this
        }

        @PackageScope
        GitHubLibActivityCheckerConfigRedundancyCheckResult build() {
            return new GitHubLibActivityCheckerConfigRedundancyCheckResult(unusedLocalGitHubMappingKeys)
        }
    }
}