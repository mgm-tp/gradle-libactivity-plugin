package com.mgmtp.gradle.libactivity.plugin.result.format.formatter.plaintext

import com.mgmtp.gradle.libactivity.plugin.result.data.AbstractCheckResult
import com.mgmtp.gradle.libactivity.plugin.result.format.formatter.CheckResultFormatter

interface PlainTextCheckResultFormatter<R extends AbstractCheckResult> extends CheckResultFormatter<String,R> { }