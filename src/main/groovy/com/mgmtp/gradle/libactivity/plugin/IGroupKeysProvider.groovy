package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope

/**
 * Provider for keys that identify groups. When grouping elements, each of them can be assigned to multiple groups.
 * The provider returns a collection of keys for these groups.
 * <p>
 * <u>Example</u>: A group keys provider might be a categorizer that returns keys for all categories in which an element
 * falls.
 * </p>
 *
 * @param <K>
 *     group key type - Note that in case of a chained grouping with different types this type parameter / upper bound
 *     needs to be raised.
 * @param <E>
 *     element type to group
 */
@PackageScope
interface IGroupKeysProvider<K, E> {

    /**
     * @param superGroupKeys The list of keys from super groups to which the element has already been assigned in case
     * of a chained grouping. May contain {@code null} if the element did not fit a group.
     * @param element The element that should be assigned a group.
     *
     * @return Set of keys for groups to which the passed element can be assigned. The assignment may depend on
     * keys from super groups to which the same element might have been assigned previously if it was grouped in a
     * chained manner.
     * <p>
     *     Returns an empty set or {@code null} only inside the set if the element cannot be assigned to a group.
     * </p>
     */
    Set<K> getGroupKeys(List<K> superGroupKeys, E element)
}