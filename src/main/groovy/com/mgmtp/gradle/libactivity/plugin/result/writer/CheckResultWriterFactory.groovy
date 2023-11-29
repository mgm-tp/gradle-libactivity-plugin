package com.mgmtp.gradle.libactivity.plugin.result.writer

import com.mgmtp.gradle.libactivity.plugin.config.LocalConfig
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

@TupleConstructor
@VisibilityOptions(Visibility.PRIVATE)
class CheckResultWriterFactory {

    static CheckResultWriter getWriter(CheckResultOutputChannel checkResultOutputChannel, LocalConfig localConfig) {

        switch (checkResultOutputChannel) {

            case CheckResultOutputChannel.FILE:
                return new CheckResultFileWriter(localConfig.outputFile)
            case CheckResultOutputChannel.CONSOLE:
                return new CheckResultConsoleWriter()
            default:
                throw new IllegalArgumentException("No writer registered for check result output channel ${checkResultOutputChannel}")
        }
    }
}