package com.mgmtp.gradle.libactivity.plugin


import groovy.transform.EqualsAndHashCode
import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

/**
 * ID to identify a lib in a Maven repository. To check the activity status of a library group and artifact ID are sufficient.
 */
@PackageScope
@EqualsAndHashCode
@TupleConstructor(post = { NullCheck.ALL_PROPS.call(this) })
@VisibilityOptions(Visibility.PACKAGE_PRIVATE)
class MavenId implements Comparable<MavenId>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L

    final String groupId

    final String artifactId

    @Override
    int compareTo(final MavenId otherMavenId) {
        // proceeds with nested comparator if 0 / equal
        return groupId <=> otherMavenId.groupId ?:
                artifactId <=> otherMavenId.artifactId
    }

    @Override
    String toString() {
        return groupId + ':' + artifactId
    }
}