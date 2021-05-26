package com.mgmtp.gradle.libactivity.plugin.result.format.collector.json

import com.mgmtp.gradle.libactivity.plugin.result.format.collector.FormattedCheckResultCollector
import groovy.json.JsonBuilder
import groovy.json.JsonGenerator

import java.util.function.BiConsumer
import java.util.function.BinaryOperator
import java.util.function.Function
import java.util.function.Supplier

class JsonFormattedCheckResultCollector implements FormattedCheckResultCollector<List<?>,JsonBuilder> {

    @Override
    Supplier<List<?>> supplier( ) {
        return { []}
    }

    @Override
    BiConsumer<List<?>, JsonBuilder> accumulator( ) {
        return { List<?> resultList, JsonBuilder formattedCheckResult ->
            if( formattedCheckResult.content) {
                resultList.add( formattedCheckResult.content)}
            }
    }

    @Override
    BinaryOperator<List<?>> combiner( ) {
        return { List<?> resultList1, List<?> resultList2 -> resultList1.addAll( resultList2)}
    }

    @Override
    Function<List<?>, String> finisher( ) {
        return { List<?> resultList -> new JsonBuilder(
                checkResults: resultList,
                new JsonGenerator.Options( ).excludeNulls( ).excludeFieldsByName( 'contentHash', 'originalClassName').build( )).toPrettyString( )
        }
    }
}