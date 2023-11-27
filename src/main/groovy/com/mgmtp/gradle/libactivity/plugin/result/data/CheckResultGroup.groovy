package com.mgmtp.gradle.libactivity.plugin.result.data

/**
 * A group of members that share the same classification inside a check result.
 *
 * @param <GM>
 *     group meta type
 * @param <M>
 *     group member type
 */
interface CheckResultGroup<GM extends Enum<GM>, M> extends Comparable<CheckResultGroup<GM, M>> {

    /**
     * @return Meta information about the group of a check result.
     */
    GM getMeta()

    /**
     * @return Collection of members that belong to the group.
     */
    Collection<M> getMembers()
}