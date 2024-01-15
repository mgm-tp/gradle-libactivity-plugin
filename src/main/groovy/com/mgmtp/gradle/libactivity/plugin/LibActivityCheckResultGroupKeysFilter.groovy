package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

/**
 * The {@link ILibActivityCheckResultGroupKeysProvider} groups check results where a group can be considered a filtered
 * result. Sometimes it is useful to filter not only check results but the parts within. The #byXXX methods of this filter
 * take into account dependencies from respective XXX super-group keys and leave only results that are in line with those
 * keys.
 */
@PackageScope
@TupleConstructor(includeFields = true)
@VisibilityOptions(Visibility.PRIVATE)
class LibActivityCheckResultGroupKeysFilter {

    private Map<Api, DetailedApiQueryResult> apiToDetailedApiQueryResults

    private final List<Comparable<?>> superGroupKeys

    static LibActivityCheckResultGroupKeysFilter filterApiToDetailedQueryResults(
            final Map<Api, DetailedApiQueryResult> apiToDetailedApiQueryResults, final List<Comparable<?>> superGroupKeys) {
        // null keys are dropped on initialization => we must however keep zeroes as keys
        final List<Comparable<?>> superGroupKeysWithValue = superGroupKeys.findAll { final Comparable<?> comparable -> null != comparable }
        return new LibActivityCheckResultGroupKeysFilter(apiToDetailedApiQueryResults, superGroupKeysWithValue)
    }

    @PackageScope
    LibActivityCheckResultGroupKeysFilter byApi() {

        final List<Api> apiKeys = superGroupKeys.findAll { final Comparable<?> comparable -> Api.class == comparable.class } as List<Api>

        if (apiKeys) {
            this.apiToDetailedApiQueryResults = apiToDetailedApiQueryResults.findAll { final Api api, final DetailedApiQueryResult detailedApiQueryResult ->
                api in apiKeys
            }
        }

        return this
    }

    @PackageScope
    LibActivityCheckResultGroupKeysFilter byCategory() {

        final List<LibActivityCheckResult.Category> categoryKeys = superGroupKeys
                .findAll { final Comparable<?> comparable -> LibActivityCheckResult.Category.class == comparable.class } as List<LibActivityCheckResult.Category>

        if (categoryKeys) {
            this.apiToDetailedApiQueryResults = apiToDetailedApiQueryResults.findAll { final Api api, final DetailedApiQueryResult detailedApiQueryResult ->
                detailedApiQueryResult.queryResult.category in categoryKeys
            }
        }

        return this
    }

    @PackageScope
    LibActivityCheckResultGroupKeysFilter byActiveCategory() {

        final LibActivityCheckResult.Category categoryKey = superGroupKeys
                .find { final Comparable<?> comparable -> LibActivityCheckResult.Category.class == comparable.class } as LibActivityCheckResult.Category

        if (LibActivityCheckResult.Category.ACTIVE == categoryKey) {
            this.apiToDetailedApiQueryResults = apiToDetailedApiQueryResults.findAll { final Api api, final DetailedApiQueryResult detailedApiQueryResult ->
                LibActivityCheckResult.Category.ACTIVE == detailedApiQueryResult.queryResult.category
            }
        } else {
            // if we are in another category group then do not disclose any query result
            apiToDetailedApiQueryResults = Map.of()
        }

        return this
    }

    @PackageScope
    LibActivityCheckResultGroupKeysFilter byQueryResult() {

        final List<ApiQueryResult> queryResultKeys = superGroupKeys
                .findAll { final Comparable<?> comparable -> ApiQueryResult.class == comparable.class } as List<ApiQueryResult>

        if (queryResultKeys) {
            this.apiToDetailedApiQueryResults = apiToDetailedApiQueryResults.findAll { final Api api, final DetailedApiQueryResult detailedApiQueryResult ->
                detailedApiQueryResult.queryResult in queryResultKeys
            }
        }

        return this
    }

    @PackageScope
    LibActivityCheckResultGroupKeysFilter byDetail() {

        final Set<Comparable<?>> detailValueKeys = superGroupKeys.findAll { final Comparable<?> comparable ->
            ApiQueryResultDetail.values().any { final ApiQueryResultDetail detail -> detail.clazz.isAssignableFrom(comparable.class) }
        }

        if (detailValueKeys) {
            this.apiToDetailedApiQueryResults = apiToDetailedApiQueryResults.findAll { final Api api, final DetailedApiQueryResult detailedApiQueryResult ->
                detailValueKeys.any { final Comparable<?> detailValueKey -> detailValueKey in detailedApiQueryResult.details.values() }
            }
        }

        return this
    }

    /**
     * @return Filtered result.
     */
    @PackageScope
    Map<Api, DetailedApiQueryResult> getResult() {
        return new HashMap<>(apiToDetailedApiQueryResults)
    }
}