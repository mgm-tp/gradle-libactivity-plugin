package com.mgmtp.gradle.libactivity.plugin.result.data

import com.mgmtp.gradle.libactivity.plugin.util.NullCheck
import groovy.transform.TupleConstructor

/**
 * Check results have a name and a group of members.
 *
 * @param <G>  group type
 */
@TupleConstructor(post = { NullCheck.ALL_PROPS.call(this) })
abstract class AbstractCheckResult<G extends AbstractCheckResultGroup> {

    final String name

    final Collection<G> groups
}