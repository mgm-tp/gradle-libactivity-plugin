package com.mgmtp.gradle.libactivity.plugin.result.format.collector.plaintext

import com.mgmtp.gradle.libactivity.plugin.result.format.collector.FormattedCheckResultCollector

import java.util.function.BiConsumer
import java.util.function.BinaryOperator
import java.util.function.Function
import java.util.function.Supplier

class PlainTextFormattedCheckResultCollector implements FormattedCheckResultCollector<String,StringJoiner> {

    @Override
    Supplier<StringJoiner> supplier( ) {
        return { new StringJoiner( System.lineSeparator( ) * 4)}
    }

    @Override
    BiConsumer<StringJoiner, String> accumulator( ) {
        return { StringJoiner joiner, String formattedCheckResult ->
            if( formattedCheckResult) {
                joiner.add( formattedCheckResult)
            }
        }
    }

    @Override
    BinaryOperator<StringJoiner> combiner( ) {
        return { StringJoiner joiner1, StringJoiner joiner2 -> joiner1.merge( joiner2)}
    }

    @Override
    Function<StringJoiner, String> finisher( ) {
        return { StringJoiner joiner -> joiner as String}
    }
}