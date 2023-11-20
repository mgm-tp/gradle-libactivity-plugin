package com.mgmtp.gradle.libactivity.plugin.result.writer

import com.mgmtp.gradle.libactivity.plugin.config.LocalConfig
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

@TupleConstructor
@VisibilityOptions(Visibility.PRIVATE)
class CheckResultWriterFactory {

    static CheckResultWriter getWriter(LocalConfig localConfig) {
        return localConfig.outputChannel == CheckResultOutputChannel.CONSOLE ? getConsoleWriter(localConfig) : getFileWriter(localConfig)
    }

    private static CheckResultWriter getConsoleWriter(LocalConfig localConfig) {
        return localConfig.outputChannel.writerClazz.getDeclaredConstructor().newInstance()
    }

    private static CheckResultWriter getFileWriter(LocalConfig localConfig) {
        return localConfig.outputChannel.writerClazz.getDeclaredConstructor(File.class).newInstance(localConfig.outputFile)
    }
}