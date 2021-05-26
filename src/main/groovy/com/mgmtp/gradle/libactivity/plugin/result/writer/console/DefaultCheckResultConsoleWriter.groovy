package com.mgmtp.gradle.libactivity.plugin.result.writer.console

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

class DefaultCheckResultConsoleWriter implements CheckResultConsoleWriter {

    private static final Logger LOGGER = Logging.getLogger( DefaultCheckResultConsoleWriter.class)

    @Override
    void write( String text) {
        LOGGER.quiet( text)
    }
}