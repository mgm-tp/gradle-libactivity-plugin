package com.mgmtp.gradle.libactivity.plugin.result.format.collector

import java.util.stream.Collector

/** Collector to aggregate formatted check results into a final string representation. Results should be processed only if they are not empty. */
trait FormattedCheckResultCollector<T,A> implements Collector<T,A,String> {

    @Override
    Set<Characteristics> characteristics( ) {
        return []
    }
}