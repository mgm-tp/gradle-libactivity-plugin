package com.mgmtp.gradle.libactivity.plugin.data.lib

import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

/** Possible tags to define a check result. */
@TupleConstructor
@VisibilityOptions( Visibility.PRIVATE)
enum LibTag {

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