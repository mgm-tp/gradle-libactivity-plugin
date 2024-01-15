package com.mgmtp.gradle.libactivity.plugin

import groovy.json.JsonGenerator
import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

@PackageScope
@TupleConstructor
@VisibilityOptions(Visibility.PRIVATE)
class CheckResultJsonGenerator {

    @PackageScope
    static final JsonGenerator INSTANCE = new JsonGenerator.Options()
            .excludeNulls()
            .excludeFieldsByName('contentHash', 'originalClassName')
            .build()
}