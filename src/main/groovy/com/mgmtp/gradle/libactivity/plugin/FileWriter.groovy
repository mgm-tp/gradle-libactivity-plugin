package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope
import groovy.transform.TupleConstructor

@PackageScope
@TupleConstructor(post = { NullCheck.ALL_PROPS.call(this) })
class FileWriter implements IWriter {

    final File targetFile

    @Override
    void write(final String content) {
        targetFile.write(content)
    }
}