package com.mgmtp.gradle.libactivity.plugin.data.lib

import com.mgmtp.gradle.libactivity.plugin.util.NullCheck
import groovy.transform.EqualsAndHashCode
import groovy.transform.TupleConstructor

/** We use g:a:v string representation to sort coordinates. */
@EqualsAndHashCode
@TupleConstructor( post = { NullCheck.ALL_PROPS.call( this)})
class LibCoordinates implements Comparable<LibCoordinates>, Serializable {

    final String groupId

    final String artifactId

    final String version

    @Override
    int compareTo( LibCoordinates otherCoordinates) {
        return toString( ) <=> otherCoordinates as String
    }

    @Override
    String toString( ) {
        return [groupId, artifactId, version].join( ':')
    }
}