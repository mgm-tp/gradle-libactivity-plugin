package com.mgmtp.gradle.libactivity.plugin.data.lib

import com.mgmtp.gradle.libactivity.plugin.util.NullCheck
import groovy.transform.EqualsAndHashCode
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

/**
 * A lib consists of
 * <ul>
 *     <li>Maven Identifier (coordinate GAV-triple)</li>
 *     <li>tags used to match a lib to a result group</li>
 *     <li>group specific details, each of them from one result group</li>
 * </ul>
 */
@EqualsAndHashCode
@VisibilityOptions(Visibility.PRIVATE)
@TupleConstructor(post = { NullCheck.ALL_PROPS.call(this) })
class Lib implements Comparable<Lib> {

    final MavenIdentifier mavenIdentifier

    final Collection<Tag> tags

    final Map<Detail, ?> details

    static Lib fromMavenIdentifier(MavenIdentifier mavenIdentifier) {
        return new Lib(mavenIdentifier, [], [:])
    }

    @Override
    String toString() {
        return this.mavenIdentifier as String
    }

    @Override
    int compareTo(Lib anotherLib) {
        return toString() <=> anotherLib as String
    }

    /** Possible tags to define a check result. */
    @TupleConstructor
    @VisibilityOptions(Visibility.PRIVATE)
    enum Tag {

        ACTIVE,
        GITHUB_RESPONSE_404,
        AT_LEAST_1_COMMIT,
        INACTIVE,
        GITHUB_RESPONSE_403,
        MOVED,
        NO_COMMITS,
        NO_GITHUB_MAPPING,
        NO_GITHUB_HOSTING,
        OUTDATED_VERSION,
        OUTDATED_RELEASE,
        RELEASE_OK,
        UNAVAILABLE_RESULT,
        UNKNOWN,
        UNKNOWN_VERSION
    }

    /** Details allowed in a result group. */
    @TupleConstructor
    @VisibilityOptions(Visibility.PRIVATE)
    enum Detail {

        NUM_COMMITS('number of commits'),
        LATEST_RELEASE_AGE('years since latest release'),
        VERSION_AGE('version age in years'),
        NEW_ADDRESS('new address')

        final String description
    }
}