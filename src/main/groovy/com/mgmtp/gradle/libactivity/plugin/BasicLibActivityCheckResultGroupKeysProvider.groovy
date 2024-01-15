package com.mgmtp.gradle.libactivity.plugin


import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

@TupleConstructor
@VisibilityOptions(Visibility.PRIVATE)
enum BasicLibActivityCheckResultGroupKeysProvider implements ILibActivityCheckResultGroupKeysProvider {

    MAVEN_ID({ final List<Comparable<?>> superGroupKeys, final LibActivityCheckResult checkResult -> Set.of(checkResult.mavenId) }),

    CATEGORY({ final List<Comparable<?>> superGroupKeys, final LibActivityCheckResult checkResult -> Set.of(checkResult.category) }),

    API({ final List<Comparable<?>> superGroupKeys, final LibActivityCheckResult checkResult -> checkResult.detailedApiQueryResults.keySet() }),

    QUERY_RESULT_FROM_API({ final List<Comparable<?>> superGroupKeys, final LibActivityCheckResult checkResult ->

        LibActivityCheckResultGroupKeysFilter.filterApiToDetailedQueryResults(checkResult.detailedApiQueryResults, superGroupKeys)
                .byApi()
                .getResult()
                .values()
                .collect { final DetailedApiQueryResult detailedApiQueryResult -> detailedApiQueryResult.queryResult }
                .toSet()
    }),

    /**
     * This is something like a sub-categorizer for the ACTIVE category. It provides group keys / query results only if
     * they belong to the active category. Any other category super-group will have no query results in that case which
     * prevents elements to appear under different query results (e.g. INACTIVE cat.: element has one query result with
     * INACTIVE and one with UNKNOWN => the latter is dropped). If more detail is necessary use {@link #QUERY_RESULT_FROM_API}
     * instead of OR after this provider.
     */
    QUERY_RESULT_FROM_ACTIVE_CATEGORY({ final List<Comparable<?>> superGroupKeys, final LibActivityCheckResult checkResult ->

        LibActivityCheckResultGroupKeysFilter.filterApiToDetailedQueryResults(checkResult.detailedApiQueryResults, superGroupKeys)
                .byActiveCategory()
                .getResult()
                .values()
                .collect { final DetailedApiQueryResult detailedApiQueryResult -> detailedApiQueryResult.queryResult }
                .toSet()
    }),

    /**
     * Denotes a check that was provided by a cache hit.
     */
    FROM_CACHE({ final List<Comparable<?>> superGroupKeys, final LibActivityCheckResult checkResult ->
        Set.of(checkResult.fromCache)
    })

    final Closure<Set<Comparable<?>>> groupKeysProvider

    @Override
    Set<Comparable<?>> getGroupKeys(final List<Comparable<?>> superGroupKeys, final LibActivityCheckResult checkResult) {
        return groupKeysProvider.call(superGroupKeys, checkResult)
    }
}