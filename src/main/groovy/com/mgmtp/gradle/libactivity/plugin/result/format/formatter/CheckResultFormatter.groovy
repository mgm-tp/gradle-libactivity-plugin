package com.mgmtp.gradle.libactivity.plugin.result.format.formatter

import com.mgmtp.gradle.libactivity.plugin.result.data.AbstractCheckResult

/**
 * Takes a check result and turns it into a formatted type.
 *
 * @param <F>  format type produced
 * @param <R>  check result type
 */
interface CheckResultFormatter<F, R extends AbstractCheckResult> {

    F format(R checkResult)
}