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
class SonatypeQueryResultCacheData implements IApiQueryResultCacheData, Serializable {

    @Serial
    private static final long serialVersionUID = 1L

    final Long latestCentralReleaseTimestamp

    @Override
    Long getLatestActivityTimestamp() {
        return latestCentralReleaseTimestamp
    }

    @PackageScope
    static Builder builder() {
        return new Builder()
    }

    @PackageScope
    @TupleConstructor
    @VisibilityOptions(Visibility.PRIVATE)
    static class Builder {

        private Long latestCentralReleaseTimestamp

        @PackageScope
        Builder latestCentralReleaseTimestamp(final Long latestCentralReleaseTimestamp) {
            this.latestCentralReleaseTimestamp = latestCentralReleaseTimestamp
            return this
        }

        @PackageScope
        SonatypeQueryResultCacheData build() {
            return new SonatypeQueryResultCacheData(latestCentralReleaseTimestamp)
        }
    }
}