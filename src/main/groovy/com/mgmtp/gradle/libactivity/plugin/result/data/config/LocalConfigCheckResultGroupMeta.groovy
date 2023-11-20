package com.mgmtp.gradle.libactivity.plugin.result.data.config

import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

/** Meta info for local config check result. */
@TupleConstructor
@VisibilityOptions(Visibility.PRIVATE)
enum LocalConfigCheckResultGroupMeta {

    UNUSED_LOCAL_GIT_HUB_MAPPING_KEYS('unused local GitHub mapping keys'),
    UNUSED_XCLUDES('unused xcludes'),
    UNUSED_XCLUDE_PATTERNS('unused xclude patterns')

    final String name

    @Override
    String toString() {
        return name
    }
}