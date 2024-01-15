package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

@PackageScope
class ConsoleWriter implements IWriter {

    private static final Logger LOGGER = Logging.getLogger(ConsoleWriter.class)

    @Override
    void write(final String content) {
        LOGGER.quiet(System.lineSeparator())
        LOGGER.quiet(content)
        LOGGER.quiet(System.lineSeparator())
    }
}