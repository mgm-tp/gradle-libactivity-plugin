package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility
import org.apache.commons.lang3.Range

@PackageScope
@TupleConstructor(post = { NullCheck.ALL_PROPS.call(this) })
@VisibilityOptions(Visibility.PRIVATE)
class Iso8601DateStringRange {

    final String minimum

    final String maximum

    @PackageScope
    static Iso8601DateStringRange fromRangeOfMillis(final Range<Long> epochMilliRange) {
        return new Iso8601DateStringRange(
                TimeConverter.epochMilliToIso8601String(epochMilliRange.minimum),
                TimeConverter.epochMilliToIso8601String(epochMilliRange.maximum))
    }

    @Override
    String toString() {
        return getMinimum() + ' .. ' + getMaximum()
    }
}