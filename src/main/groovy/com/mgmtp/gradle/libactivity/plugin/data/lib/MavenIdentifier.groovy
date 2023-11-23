package com.mgmtp.gradle.libactivity.plugin.data.lib

import com.mgmtp.gradle.libactivity.plugin.util.NullCheck
import groovy.transform.EqualsAndHashCode
import groovy.transform.TupleConstructor

/** We use g:a:v string representation to sort libraries. */
@EqualsAndHashCode
@TupleConstructor(post = { NullCheck.ALL_PROPS.call(this) })
class MavenIdentifier implements Comparable<MavenIdentifier>, Serializable {

    final String groupId

    final String artifactId

    final String version

    @Override
    int compareTo(MavenIdentifier otherIdentifier) {
        return toString() <=> otherIdentifier as String
    }

    @Override
    String toString() {
        return [groupId, artifactId, version].join(':')
    }
}