package com.mgmtp.gradle.libactivity.plugin.result.format.formatter.plaintext

import com.mgmtp.gradle.libactivity.plugin.result.data.CheckResult
import com.mgmtp.gradle.libactivity.plugin.result.data.CheckResultGroup
import com.mgmtp.gradle.libactivity.plugin.result.format.formatter.CheckResultFormatter

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
interface PlainTextCheckResultFormatter<GM extends Enum<GM>, M, G extends CheckResultGroup<GM, M>, R extends CheckResult<GM, M, G>>
        extends CheckResultFormatter<String, GM, M, G, R> {}