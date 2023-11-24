package com.mgmtp.gradle.libactivity.plugin.result.writer

import com.mgmtp.gradle.libactivity.plugin.result.writer.console.DefaultCheckResultConsoleWriter
import com.mgmtp.gradle.libactivity.plugin.result.writer.dual.CheckResultDualWriter
import com.mgmtp.gradle.libactivity.plugin.result.writer.file.DefaultCheckResultFileWriter
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

@TupleConstructor
@VisibilityOptions(Visibility.PRIVATE)
enum CheckResultOutputChannel {

    CONSOLE(DefaultCheckResultConsoleWriter.class),
    FILE(DefaultCheckResultFileWriter.class),
    DUAL(CheckResultDualWriter.class)

    final Class<CheckResultWriter> writerClazz

    static CheckResultOutputChannel parse(String channel) {
        return Optional.ofNullable(values().find { CheckResultOutputChannel outputChannel -> outputChannel.name().equalsIgnoreCase(channel) })
                .orElseThrow { new IllegalArgumentException("Invalid output channel: ${channel} ---> Valid options: ${values()}") }
    }
}