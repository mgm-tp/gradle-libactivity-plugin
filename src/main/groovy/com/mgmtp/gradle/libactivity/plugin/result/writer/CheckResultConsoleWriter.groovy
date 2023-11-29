package com.mgmtp.gradle.libactivity.plugin.result.writer


import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

class CheckResultConsoleWriter implements CheckResultWriter {

    private static final Logger LOGGER = Logging.getLogger(CheckResultConsoleWriter.class)

    @Override
    void write(String text) {
        LOGGER.quiet(text)
    }
}