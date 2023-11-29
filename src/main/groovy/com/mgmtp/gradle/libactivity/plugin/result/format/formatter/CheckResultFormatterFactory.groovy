package com.mgmtp.gradle.libactivity.plugin.result.format.formatter

import com.mgmtp.gradle.libactivity.plugin.result.data.CheckResult
import com.mgmtp.gradle.libactivity.plugin.result.data.CheckResultGroup
import com.mgmtp.gradle.libactivity.plugin.result.data.config.LocalConfigCheckResult
import com.mgmtp.gradle.libactivity.plugin.result.data.lib.LibCheckResult
import com.mgmtp.gradle.libactivity.plugin.result.format.CheckResultOutputFormat
import com.mgmtp.gradle.libactivity.plugin.result.format.formatter.json.JsonLibCheckResultFormatter
import com.mgmtp.gradle.libactivity.plugin.result.format.formatter.json.JsonLocalConfigCheckResultFormatter
import com.mgmtp.gradle.libactivity.plugin.result.format.formatter.plaintext.PlainTextLibCheckResultFormatter
import com.mgmtp.gradle.libactivity.plugin.result.format.formatter.plaintext.PlainTextLocalConfigCheckResultFormatter
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

@TupleConstructor
@VisibilityOptions(Visibility.PRIVATE)
class CheckResultFormatterFactory {

    static <F, GM extends Enum<GM>, M, G extends CheckResultGroup<GM, M>, R extends CheckResult> CheckResultFormatter<F, GM, M, G, R> getFormatter(
            CheckResultOutputFormat outputFormat, Class<CheckResult> checkResultClazz) {

        CheckResultOutput checkResultOutput = CheckResultOutput.get(outputFormat, checkResultClazz)

        switch (checkResultOutput) {

            case CheckResultOutput.JSON_LIB_CHECK_RESULT:
                return new JsonLibCheckResultFormatter()
            case CheckResultOutput.JSON_LOCAL_CONFIG_CHECK_RESULT:
                return new JsonLocalConfigCheckResultFormatter()
            case CheckResultOutput.PLAIN_TEXT_LIB_CHECK_RESULT:
                return new PlainTextLibCheckResultFormatter()
            case CheckResultOutput.PLAIN_TEXT_LOCAL_CONFIG_CHECK_RESULT:
                return new PlainTextLocalConfigCheckResultFormatter()
            default:
                throw new IllegalArgumentException("No formatter registered for check result output '${checkResultOutput}'")
        }
    }

    @TupleConstructor
    @VisibilityOptions(Visibility.PRIVATE)
    private static enum CheckResultOutput {

        JSON_LIB_CHECK_RESULT(CheckResultOutputFormat.JSON, LibCheckResult.class),
        JSON_LOCAL_CONFIG_CHECK_RESULT(CheckResultOutputFormat.JSON, LocalConfigCheckResult.class),
        PLAIN_TEXT_LIB_CHECK_RESULT(CheckResultOutputFormat.TXT, LibCheckResult.class),
        PLAIN_TEXT_LOCAL_CONFIG_CHECK_RESULT(CheckResultOutputFormat.TXT, LocalConfigCheckResult.class)

        CheckResultOutputFormat outputFormat
        Class<CheckResult> checkResultClazz

        private static CheckResultOutput get(CheckResultOutputFormat outputFormat, Class<CheckResult> checkResultClazz) {

            CheckResultOutput checkResultOutput = values().find { CheckResultOutput checkResultOutput ->
                checkResultOutput.outputFormat == outputFormat && checkResultOutput.checkResultClazz == checkResultClazz
            }

            if (checkResultOutput) {
                return checkResultOutput
            }

            throw new IllegalArgumentException("No check result output available for format '${outputFormat}' and result class '${checkResultClazz}'")
        }
    }
}