package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope
import groovy.transform.ToString
import groovy.transform.builder.Builder
import org.apache.commons.lang3.LongRange
import org.apache.commons.lang3.Range

import java.time.Duration

@PackageScope
@ToString
class ActivityTimeframeConfig {

    final int maxDaysSinceLatestActivity

    final Range<Long> timeframe

    private Iso8601DateStringRange timeframeIso8601

    @Builder
    private ActivityTimeframeConfig(final long referenceTimestampEpochMilli, final int maxDaysSinceLatestActivity) {

        validateMaxDaysSinceLatestActivity(maxDaysSinceLatestActivity)

        this.maxDaysSinceLatestActivity = maxDaysSinceLatestActivity
        timeframe = createTimeframe(referenceTimestampEpochMilli, maxDaysSinceLatestActivity)
    }

    @PackageScope
    Iso8601DateStringRange getTimeframeIso8601() {
        // not necessarily needed so we init on request
        if (!timeframeIso8601) {
            timeframeIso8601 = Iso8601DateStringRange.fromRangeOfMillis(timeframe)
        }
        return timeframeIso8601
    }

    @Override
    String toString() {
        'maxDaysSinceLatestActivity: ' + maxDaysSinceLatestActivity + '; timeframe: ' + timeframe
    }

    private static void validateMaxDaysSinceLatestActivity(final int maxDaysSinceLatestActivity) {
        if (maxDaysSinceLatestActivity < 1) {
            throw new IllegalArgumentException("maxDaysSinceLatestActivity must be positive. Received: ${maxDaysSinceLatestActivity}")
        }
    }

    private static Range createTimeframe(final long referenceTimestampEpochMilli, final int maxDaysTolerance) {
        return LongRange.of(referenceTimestampEpochMilli - Duration.ofDays(maxDaysTolerance).toMillis(), referenceTimestampEpochMilli)
    }
}