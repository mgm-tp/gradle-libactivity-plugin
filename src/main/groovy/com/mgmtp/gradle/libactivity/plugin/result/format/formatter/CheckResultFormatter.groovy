package com.mgmtp.gradle.libactivity.plugin.result.format.formatter

import com.mgmtp.gradle.libactivity.plugin.result.data.CheckResult
import com.mgmtp.gradle.libactivity.plugin.result.data.CheckResultGroup

/**
 * Takes a check result and turns it into a formatted type.
 *
 * @param <F>
 *     format type produced
 * @param <GM>
 *     group meta type
 * @param <M>
 *     group member type
 * @param <G>
 *     group type
 * @param <R>
 *     check result type
 */
interface CheckResultFormatter<F, GM extends Enum<GM>, M, G extends CheckResultGroup<GM, M>, R extends CheckResult<GM, M, G>> {

    F format(R checkResult)
}