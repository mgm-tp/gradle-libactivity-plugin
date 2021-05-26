package com.mgmtp.gradle.libactivity.plugin.result.format.formatter.json

import com.mgmtp.gradle.libactivity.plugin.result.data.AbstractCheckResult
import com.mgmtp.gradle.libactivity.plugin.result.format.formatter.CheckResultFormatter
import groovy.json.JsonBuilder

interface JsonCheckResultFormatter<R extends AbstractCheckResult> extends CheckResultFormatter<JsonBuilder,R> { }
