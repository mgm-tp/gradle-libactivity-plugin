package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.EqualsAndHashCode
import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

import javax.annotation.Nullable

@PackageScope
@EqualsAndHashCode
@TupleConstructor
@VisibilityOptions(Visibility.PRIVATE)
class GitHubLibActivityCheckerConfigCacheData implements IApiLibActivityCheckerConfigCacheData, Serializable {

    @Serial
    private static final long serialVersionUID = 1L

    @Nullable
    final String localGitHubMappingKey

    @Nullable
    final String localGitHubMappingValue

    @PackageScope
    static Builder builder() {
        return new Builder()
    }

    @PackageScope
    @TupleConstructor
    @VisibilityOptions(Visibility.PRIVATE)
    // explicitly referencing an AST builder class makes the compiler unable to resolve this class
    static class Builder {

        @Nullable
        private String localGitHubMappingKey

        @Nullable
        private String localGitHubMappingValue

        @PackageScope
        Builder localGitHubMappingKey(final String localGitHubMappingKey) {
            this.localGitHubMappingKey = localGitHubMappingKey
            return this
        }

        @PackageScope
        Builder localGitHubMappingValue(final String localGitHubMappingValue) {
            this.localGitHubMappingValue = localGitHubMappingValue
            return this
        }

        @PackageScope
        GitHubLibActivityCheckerConfigCacheData build() {
            return new GitHubLibActivityCheckerConfigCacheData(localGitHubMappingKey, localGitHubMappingValue)
        }
    }
}