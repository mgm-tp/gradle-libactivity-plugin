package com.mgmtp.gradle.libactivity.plugin.result.data

import com.mgmtp.gradle.libactivity.plugin.util.NullCheck
import groovy.transform.TupleConstructor

/**
 * Check results have a name and a group of members.
 *
 * @param <GM>
 *     group meta type
 * @param <M>
 *     group member type
 * @param <G>
 *     group type
 */
@TupleConstructor(post = { NullCheck.ALL_PROPS.call(this) })
abstract class AbstractCheckResult<GM extends Enum<GM>, M, G extends CheckResultGroup<GM, M>> implements CheckResult<GM, M, G> {

    final String name

    final Collection<G> groups
}