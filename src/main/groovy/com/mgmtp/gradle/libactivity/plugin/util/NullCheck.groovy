package com.mgmtp.gradle.libactivity.plugin.util

import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

/** Utility iterating over properties from the passed instance to do a null check. */
@TupleConstructor
@VisibilityOptions( Visibility.PRIVATE)
class NullCheck {

    static Closure<Map<?,?>> ALL_PROPS = {
        Object instance -> instance.properties.each {
            Map.Entry<?,?> property -> Optional.ofNullable( property.value)
                    .orElseThrow{ new NullPointerException( "Property ${ instance.class.name}#${ property.key} must not be null initialized")}
        }
    }
}