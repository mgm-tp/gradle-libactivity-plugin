package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

import java.time.*

@PackageScope
@TupleConstructor
@VisibilityOptions(Visibility.PRIVATE)
final class TimeConverter {

    /**
     * @return ISO-8601 string in milliseconds since the epoch 1970-01-01T00:00:00Z (UTC time).
     * @throws java.time.format.DateTimeParseException If parsing cannot deliver ISO-8601 datetime
     */
    @PackageScope
    static long iso8601StringToEpochMilli(final String iso8601String) {
        final long epochSecond = ZonedDateTime.parse(iso8601String).toEpochSecond()
        return Duration.ofSeconds(epochSecond).toMillis()
    }

    @PackageScope
    static String epochMilliToIso8601String(final long epochMilli) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.of(ZoneOffset.UTC.id)).toString()
    }

    @PackageScope
    static long rangeOfMillisToDays(final long fromMillis, final long toMillis) {
        return Duration.ofMillis(toMillis - fromMillis).toDays()
    }
}