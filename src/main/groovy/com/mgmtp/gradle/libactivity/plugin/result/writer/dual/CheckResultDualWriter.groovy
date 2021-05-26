package com.mgmtp.gradle.libactivity.plugin.result.writer.dual

import com.mgmtp.gradle.libactivity.plugin.result.writer.console.CheckResultConsoleWriter
import com.mgmtp.gradle.libactivity.plugin.result.writer.console.DefaultCheckResultConsoleWriter
import com.mgmtp.gradle.libactivity.plugin.result.writer.file.CheckResultFileWriter
import com.mgmtp.gradle.libactivity.plugin.result.writer.file.DefaultCheckResultFileWriter

class CheckResultDualWriter implements CheckResultFileWriter, CheckResultConsoleWriter {

    final CheckResultFileWriter fileWriter
    final CheckResultConsoleWriter consoleWriter = new DefaultCheckResultConsoleWriter( )

    CheckResultDualWriter( File targetFile) {
        Objects.requireNonNull( targetFile)
        fileWriter = new DefaultCheckResultFileWriter( targetFile)
    }

    @Override
    void write( String text) {
        fileWriter.write( text)
        consoleWriter.write( text)
    }
}