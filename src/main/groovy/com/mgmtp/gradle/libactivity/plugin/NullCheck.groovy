package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

import javax.annotation.Nullable

/**
 * Alternative to Groovy's {@link @NullCheck} that checks all explicit constructors and methods when placed at class.
 * The check here only verifies that all properties have a non-{@code null} value.
 */
@PackageScope
@TupleConstructor
@VisibilityOptions(Visibility.PRIVATE)
final class NullCheck {

    /**
     * Verifies the target instance has only non-{@code null} properties including those from superclasses.
     */
    @PackageScope
    static Closure<Map<String, Object>> ALL_PROPS = { final Object instance -> checkNotNull(instance) }

    private static void checkNotNull(final Object instance, final String... targetPropNames) {

        final Map<String, Object> targetProps = targetPropNames ? findAllMatchingProps(instance, targetPropNames) : instance.properties
        final Map.Entry<Object, Object> firstNullEntry = targetProps.find { final Object propName, final Object propValue -> null == propValue }

        if (firstNullEntry) {
            throw new NullPointerException("Property ${instance.class.name}#${firstNullEntry.key.toString()} must not be null initialized")
        }
    }

    private static Map<Object, Object> findAllMatchingProps(final Object instance, final String... targetPropNames) {
        return instance.properties.findAll { final Object propName, final Object propValue -> propName.toString() in targetPropNames }
    }

    @PackageScope
    static void map(@Nullable final Map<?, ?> map) {

        if (null == map) {
            throw new NullPointerException('map is null')
        }

        final boolean containsNull = map.any { final Object key, final Object value -> null == key || null == value }

        if (containsNull) {
            throw new NullPointerException('null in map')
        }
    }

    @PackageScope
    static void collection(@Nullable final Collection<?> collection) {

        if (null == collection) {
            throw new NullPointerException('collection is null')
        }

        final boolean containsNull = collection.any { final Object element -> null == element }

        if (containsNull) {
            throw new NullPointerException('null in collection')
        }
    }
}