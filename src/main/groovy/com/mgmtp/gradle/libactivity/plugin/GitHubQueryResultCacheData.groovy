package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.EqualsAndHashCode
import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

@PackageScope
@EqualsAndHashCode
@TupleConstructor(post = { NullCheck.ALL_PROPS.call(this) })
@VisibilityOptions(Visibility.PRIVATE)
class GitHubQueryResultCacheData implements IApiQueryResultCacheData, Serializable {

    @Serial
    private static final long serialVersionUID = 1L

    final Long latestCommitTimestamp

    @Override
    Long getLatestActivityTimestamp() {
        return latestCommitTimestamp
    }

    @PackageScope
    static Builder builder() {
        return new Builder()
    }

    @PackageScope
    @TupleConstructor
    @VisibilityOptions(Visibility.PRIVATE)
    static class Builder {

        private Long latestCommitTimestamp

        @PackageScope
        Builder latestCommitTimestamp(final Long latestCommitTimestamp) {
            this.latestCommitTimestamp = latestCommitTimestamp
            return this
        }

        @PackageScope
        GitHubQueryResultCacheData build() {
            return new GitHubQueryResultCacheData(latestCommitTimestamp)
        }
    }
}