package com.mgmtp.gradle.libactivity.plugin.data.lib

import com.mgmtp.gradle.libactivity.plugin.util.NullCheck
import groovy.transform.EqualsAndHashCode
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

/**
 * A lib consists of
 * <ul>
 *     <li>coordinate triple (gav)</li>
 *     <li>tags used to match a lib to a result group</li>
 *     <li>group specific details, each of them from one result group</li>
 * </ul>
 */
@EqualsAndHashCode
@VisibilityOptions(Visibility.PRIVATE)
@TupleConstructor(post = { NullCheck.ALL_PROPS.call(this) })
class Lib implements Comparable<Lib> {

    final LibCoordinates coordinates

    final Collection<LibTag> tags

    final Map<LibDetail, ?> details

    static Lib fromCoordinates(LibCoordinates coordinates) {
        return new Lib(coordinates, [], [:])
    }

    @Override
    String toString() {
        return this.coordinates as String
    }

    @Override
    int compareTo(Lib anotherLib) {
        return toString() <=> anotherLib as String
    }
}