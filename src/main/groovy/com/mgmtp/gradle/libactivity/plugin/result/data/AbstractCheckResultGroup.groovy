package com.mgmtp.gradle.libactivity.plugin.result.data

import com.mgmtp.gradle.libactivity.plugin.util.NullCheck
import groovy.transform.EqualsAndHashCode
import groovy.transform.TupleConstructor

/**
 * Check results groups have metadata and members. Simple comparability is achieved by order of meta enum instance.
 *
 * @param <M>  group member type
 * @param <GM>  group meta type
 */
@EqualsAndHashCode
@TupleConstructor(post = { NullCheck.ALL_PROPS.call(this) })
abstract class AbstractCheckResultGroup<GM extends Enum<GM>, M> implements Comparable<AbstractCheckResultGroup<GM, M>> {

    final GM meta

    final Collection<M> members

    @Override
    int compareTo(AbstractCheckResultGroup<GM, M> anotherGroup) {
        return Integer.compare(meta.ordinal(), anotherGroup.meta.ordinal())
    }
}