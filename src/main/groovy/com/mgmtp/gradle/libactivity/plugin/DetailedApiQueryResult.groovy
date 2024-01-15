package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.EqualsAndHashCode
import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

@PackageScope
@EqualsAndHashCode
@TupleConstructor(force = true, post = { NullCheck.ALL_PROPS.call(this) })
@VisibilityOptions(Visibility.PRIVATE)
class DetailedApiQueryResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L

    final ApiQueryResult queryResult

    final Map<ApiQueryResultDetail, Comparable<?>> details

    @PackageScope
    Map<ApiQueryResultDetail, Comparable<?>> getDetails() {
        return new HashMap<>(details)
    }

    @PackageScope
    Builder toBuilder() {
        return builder()
                .apiQueryResult(queryResult)
                .apiQueryResultDetails(details)
    }

    @PackageScope
    static Builder builder() {
        return new Builder()
    }

    @PackageScope
    static class Builder {

        ApiQueryResult apiQueryResult

        final Map<ApiQueryResultDetail, Comparable<?>> details = new HashMap<>()

        @PackageScope
        Builder apiQueryResult(final ApiQueryResult apiQueryResult) {
            this.apiQueryResult = apiQueryResult
            return this
        }

        @PackageScope
        Builder apiQueryResultDetail(final ApiQueryResultDetail detailKey, final Comparable<?> detailValue) {

            Objects.requireNonNull(detailKey)
            Objects.requireNonNull(detailValue)

            details[detailKey] = detailValue
            return this
        }

        @PackageScope
        Builder apiQueryResultDetails(final Map<ApiQueryResultDetail, Comparable<?>> details) {
            NullCheck.map(details)
            this.details.putAll(details)
            return this
        }

        DetailedApiQueryResult build() {
            return new DetailedApiQueryResult(apiQueryResult, details)
        }
    }
}