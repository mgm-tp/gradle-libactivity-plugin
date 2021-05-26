package com.mgmtp.gradle.libactivity.plugin.data.lib

import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

/** Details allowed in a result group. */
@TupleConstructor
@VisibilityOptions( Visibility.PRIVATE)
enum LibDetail {

    NUM_COMMITS( 'number of commits'),
    LATEST_RELEASE_AGE('years since latest release'),
    VERSION_AGE('version age in years'),
    NEW_ADDRESS('new address')

    final String description
}