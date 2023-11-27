package com.mgmtp.gradle.libactivity.plugin.result.format.formatter.json

import com.mgmtp.gradle.libactivity.plugin.result.data.CheckResult
import com.mgmtp.gradle.libactivity.plugin.result.data.CheckResultGroup
import com.mgmtp.gradle.libactivity.plugin.result.format.formatter.CheckResultFormatter
import groovy.json.JsonBuilder

/**
 * @param <GM>
 *     group meta type
 * @param <M>
 *     group member type
 * @param <G>
 *     group type
 * @param <R>
 *     check result type
 */
interface JsonCheckResultFormatter<GM extends Enum<GM>, M, G extends CheckResultGroup<GM, M>, R extends CheckResult<GM, M, G>>
        extends CheckResultFormatter<JsonBuilder, GM, M, G, R> {}