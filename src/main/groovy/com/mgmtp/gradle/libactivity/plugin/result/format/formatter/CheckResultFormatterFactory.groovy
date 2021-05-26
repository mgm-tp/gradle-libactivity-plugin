package com.mgmtp.gradle.libactivity.plugin.result.format.formatter

import com.mgmtp.gradle.libactivity.plugin.result.data.AbstractCheckResult
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
@VisibilityOptions( Visibility.PRIVATE)
class CheckResultFormatterFactory {

    static <F, R extends AbstractCheckResult> CheckResultFormatter<F,R> getFormatter( CheckResultOutputFormat outputFormat, Class<AbstractCheckResult> checkResultClazz) {
        return CheckResultOutputFormatter.getImplementingClazz( outputFormat, checkResultClazz).getDeclaredConstructor( ).newInstance( )
    }

    @TupleConstructor
    @VisibilityOptions( Visibility.PRIVATE)
    private static enum CheckResultOutputFormatter {
        JSON_LIB_CHECK_RESULT_FORMATTER( JsonLibCheckResultFormatter.class, CheckResultOutputFormat.JSON, LibCheckResult.class),
        JSON_LOCAL_CONFIG_CHECK_RESULT_FORMATTER( JsonLocalConfigCheckResultFormatter.class, CheckResultOutputFormat.JSON, LocalConfigCheckResult.class),
        PLAIN_TEXT_LIB_CHECK_RESULT_FORMATTER( PlainTextLibCheckResultFormatter.class, CheckResultOutputFormat.TXT, LibCheckResult.class),
        PLAIN_TEXT_LOCAL_CONFIG_CHECK_RESULT_FORMATTER( PlainTextLocalConfigCheckResultFormatter.class, CheckResultOutputFormat.TXT, LocalConfigCheckResult.class)

        Class<CheckResultFormatter> implementingClazz
        CheckResultOutputFormat outputFormat
        Class<AbstractCheckResult> checkResultClazz

        static Class<CheckResultFormatter> getImplementingClazz( CheckResultOutputFormat outputFormat, Class<AbstractCheckResult> checkResultClazz) {
            return Arrays.stream( values( )).filter { CheckResultOutputFormatter outputFormatter ->
                    outputFormatter.outputFormat == outputFormat && outputFormatter.checkResultClazz == checkResultClazz
                }
                .map{ CheckResultOutputFormatter outputFormatter -> outputFormatter.implementingClazz}
                .findFirst( )
                .orElseThrow{ new IllegalArgumentException( "No formatter implementation available for output format '${ outputFormat}' and check result class '${ checkResultClazz}'")}
        }
    }
}