package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

@TupleConstructor
@VisibilityOptions(Visibility.PRIVATE)
enum ApiQueryResultDetail implements ILibActivityCheckResultGroupKeysProvider {

    DAYS_SINCE_LATEST_CENTRAL_RELEASE(Long.class),
    DAYS_SINCE_LATEST_GITHUB_COMMIT(Long.class),
    RESPONSE_CODE(Integer.class)

    final Class<?> clazz

    @Override
    Set<Comparable<?>> getGroupKeys(final List<Comparable<?>> superGroupKeys, final LibActivityCheckResult element) {
        // The default filter restricts details to those that match an API and a query result but allows details from
        // query results connected to different categories. E.g. "days since" will be present for Sonatype and GitHub
        // groups even if one query result belongs to the ACTIVE and the other to the INACTIVE category.
        return LibActivityCheckResultGroupKeysFilter.filterApiToDetailedQueryResults(element.detailedApiQueryResults, superGroupKeys)
                .byApi()
                .byQueryResult()
                .getResult()
                .values()
                .collect { final DetailedApiQueryResult queryResult -> queryResult.details[this] }
                .findAll { final Comparable<?> detailValue -> null != detailValue }
                .toSet()
    }
}