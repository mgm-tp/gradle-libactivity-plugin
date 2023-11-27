package com.mgmtp.gradle.libactivity.plugin.result.data

/**
 * @param <GM>
 *     group meta type
 * @param <M>
 *     group member type
 * @param <G>
 *     group type
 */
interface CheckResult<GM extends Enum<GM>, M, G extends CheckResultGroup<GM, M>> {

    /**
     * @return Name of the check to which this result applies.
     */
    String getName()

    /**
     * @returns Collection of groups with members that share a classification inside the result.
     */
    Collection<G> getGroups()
}