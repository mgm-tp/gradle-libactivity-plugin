package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

@PackageScope
@TupleConstructor(post = { NullCheck.ALL_PROPS.call(this) })
@VisibilityOptions(Visibility.PACKAGE_PRIVATE)
class GitHubLibActivityCheckerConfigCacheDataValidator implements IApiLibActivityCheckerConfigCacheDataValidator<GitHubLibActivityCheckerConfigCacheData> {

    final Config config

    @Override
    boolean isCheckerConfigCacheDataValid(final GitHubLibActivityCheckerConfigCacheData checkerConfigCacheData) {

        final String gitHubMappingKey = checkerConfigCacheData.localGitHubMappingKey

        if (gitHubMappingKey) {

            final GitHubLibActivityCheckerConfig gitHubCheckerConfig = config.getLibActivityCheckerConfig(GitHubLibActivityCheckerConfig.class)
            // first check if the lib key is still present in the current user config => does it point to the same
            // GitHub path?
            if (gitHubCheckerConfig.localGitHubMappings.containsKey(gitHubMappingKey)) {
                return checkerConfigCacheData.localGitHubMappingValue == gitHubCheckerConfig.localGitHubMappings[gitHubMappingKey]
            }
            // if the lib key was removed by the user we need to check the global mappings from the plugin classpath => a
            // common use case might be that the user removed a self-declared mapping because it was reported redundant
            return checkerConfigCacheData.localGitHubMappingValue == gitHubCheckerConfig.globalGitHubMappings[gitHubMappingKey]
        }

        return true
    }
}