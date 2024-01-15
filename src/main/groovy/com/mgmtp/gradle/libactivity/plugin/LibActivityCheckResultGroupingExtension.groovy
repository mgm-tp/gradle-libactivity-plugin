package com.mgmtp.gradle.libactivity.plugin

/**
 * A configuration that will be used to group and sort results from the lib activity check based on a selection of
 * keys.
 * <p>
 *     There are defaults for this property which depend on the outputFormat. A default is assigned only if this
 *     property is not explicitly set by the user who configures the task.
 * </p>
 */
@groovy.transform.NullCheck
class LibActivityCheckResultGroupingExtension {



    /**
     * List of providers of keys to group lib activity check results. Multiple providers result in a sub-grouping of check
     * results from one group. E.g.: the category keys provider will group by keys that correspond to enumerations from
     * {@link LibActivityCheckResult.Category}. Adding the API query result provider will further group by enumerations from
     * {@link ApiQueryResult}. As a result there might be a group "active" with sub-group "GitHub commit found" that contains
     * matching lib activity check results.
     *
     * @see {@link BasicLibActivityCheckResultGroupKeysProvider}
     * @see {@link ApiQueryResultDetail}
     */
    void setGroupByKeysFromProviders(final List<ILibActivityCheckResultGroupKeysProvider> groupByKeysFromProviders) {
        this.groupByKeysFromProviders = groupByKeysFromProviders
    }

    /**
     * Collection of providers whose group keys should be sorted in reverse order. If a provider is not specified then the default
     * order of its keys is the natural order.
     */
    void setSortKeysFromProvidersInReverseOrder(final Collection<ILibActivityCheckResultGroupKeysProvider> sortKeysFromProvidersInReverseOrder) {
        this.sortKeysFromProvidersInReverseOrder = sortKeysFromProvidersInReverseOrder
    }
}