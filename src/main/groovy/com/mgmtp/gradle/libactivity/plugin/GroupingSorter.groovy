package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope

/**
 * Groups a collection of elements and sorts them.
 * * <p>
 *     The sorter treats group keys of type {@link Collection} such that each contained element will be used as
 *     a group key. Empty collections will result in a {@code null} group key.
 * </p>
 * <p>
 *     The sorter may group non-assignable elements, i.e. it groups elements by the {@code null} key if they do not fit
 *     a group. Respective group key providers are encouraged to return either an empty list or one with just
 *     a {@code null}-key if group assignment is not possible.
 * </p>
 */
@PackageScope
class GroupingSorter {

    final IGroupKeysProvider<Comparable<?>, ?> groupKeysProvider

    final boolean sortInReverseOrder

    final GroupingSorter subGroupingSorter

    GroupingSorter(final Map<IGroupKeysProvider<Comparable<?>, ?>, Boolean> groupKeysProviderToSortOptions) {
        this(groupKeysProviderToSortOptions, 0)
    }

    private GroupingSorter(final Map<IGroupKeysProvider<Comparable<?>, ?>, Boolean> groupKeysProviderToSortOptions, final int currentIndex) {

        verifyProviderIsPresent(groupKeysProviderToSortOptions, currentIndex)

        final Map.Entry<IGroupKeysProvider<Comparable<?>, ?>, Boolean> groupKeysProviderToSortOption = groupKeysProviderToSortOptions.entrySet()
                .toList()[currentIndex]
        groupKeysProvider = Objects.requireNonNull(groupKeysProviderToSortOption.key)
        sortInReverseOrder = Objects.requireNonNull(groupKeysProviderToSortOption.value)

        final int nextIndex = currentIndex + 1
        subGroupingSorter = nextIndex < groupKeysProviderToSortOptions.size() ? new GroupingSorter(groupKeysProviderToSortOptions, nextIndex) : null
    }

    private static void verifyProviderIsPresent(final Map<IGroupKeysProvider<Comparable<?>, ?>, Boolean> groupKeysProviderToSortOptions, final int currentIndex) {
        if (currentIndex >= groupKeysProviderToSortOptions.size()) {
            throw new IllegalArgumentException("Cannot initialize ${GroupingSorter.class.simpleName} with ${IGroupKeysProvider.class.simpleName} at non-existent position ${currentIndex}.")
        }
    }

    /**
     * @return Map with comparable keys. Values are maps that can be further nested in case of a chained grouping or
     * lists if no further grouping applies.
     */
    @PackageScope
    Map<Comparable<?>, ?> groupAndSort(final Collection<Comparable<?>> elements) {
        groupAndSortSuperGroupAware([], elements)
    }

    /**
     * @param superGroupKeys Keys of super groups in order. The 1st key belongs to the top-level group. The last key belongs
     * to the direct parent group.
     * @param elements Elements narrowed down to the parent group that should now be grouped further.
     */
    private Map<Comparable<?>, ?> groupAndSortSuperGroupAware(final List<Comparable<?>> superGroupKeys, final Collection<Comparable<?>> elements) {

        final Map<Comparable<?>, List<Comparable<?>>> groups = new HashMap<>()

        elements.each { final Comparable<?> element ->

            // empty if element in no group
            final Set<Comparable<?>> groupKeys = groupKeysProvider.getGroupKeys(superGroupKeys, element)

            if (groupKeys) {
                groupKeys.each { final Comparable<?> groupKey -> addElementToGroup(groups, groupKey, element) }
            } else {
                // could not assign a group to element
                addElementToGroup(groups, null, element)
            }
        }
        // We execute sub-grouping if wanted. After grouping is done we sort the grouped elements.
        return sortGroups(subGroupingSorter ? groupIntoSubGroups(superGroupKeys, groups) : sortGroupElements(groups))
    }

    private Map<Comparable<?>, ?> groupIntoSubGroups(final List<Comparable<?>> superGroupKeys, final Map<Comparable<?>, List<Comparable<?>>> groups) {
        return groups.collectEntries { final Comparable<?> groupKey, final List<Comparable<?>> elementsInGroup ->
            [(groupKey): subGroupingSorter.groupAndSortSuperGroupAware(superGroupKeys + groupKey, elementsInGroup)]
        }
    }

    /**
     * @return Groups sorted by group key. If a {@code null} key is present its group will come first.
     */
    private Map<Comparable<?>, ?> sortGroups(final Map<Comparable<?>, ?> groups) {
        final Comparator<Comparable<?>> groupKeysComparator = Comparator.nullsFirst(sortInReverseOrder ? Comparator.reverseOrder() : Comparator.naturalOrder())
        return groups.sort(groupKeysComparator)
    }

    /**
     * Sorts elements inside a group. This does not change the position of a group among all groups.
     */
    private static Map<Comparable<?>, List<Comparable<?>>> sortGroupElements(final Map<Comparable<?>, List<Comparable<?>>> groups) {
        // we made the list of elements mutable so it is sorted in place
        return groups.collectEntries { final Comparable<?> groupKey, final List<?> group -> [(groupKey): group.sort()] }
    }

    /**
     * @param groups The groups that hold the element target group
     * @param groupKey The key to identify the target group
     * @param element The element to add to the target group
     */
    private static void addElementToGroup(
            final Map<Comparable<?>, List<Comparable<?>>> groups, final Comparable<?> groupKey, final Comparable<?> element) {

        if (null == groups[groupKey]) {
            groups[groupKey] = [element]
        } else {
            groups[groupKey] << element
        }
    }
}