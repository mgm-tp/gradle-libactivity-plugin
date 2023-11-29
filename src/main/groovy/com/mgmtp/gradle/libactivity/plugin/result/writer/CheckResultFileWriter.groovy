package com.mgmtp.gradle.libactivity.plugin.result.writer


import com.mgmtp.gradle.libactivity.plugin.util.NullCheck
import groovy.transform.TupleConstructor

@TupleConstructor(post = { NullCheck.ALL_PROPS.call(this) })
class CheckResultFileWriter implements CheckResultWriter {

    final File targetFile

    @Override
    void write(String text) {
        targetFile.append(text)
    }
}