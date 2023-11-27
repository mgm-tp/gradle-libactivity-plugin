package com.mgmtp.gradle.libactivity.plugin.result.format.formatter.plaintext

import com.mgmtp.gradle.libactivity.plugin.result.data.config.LocalConfigCheckResult
import com.mgmtp.gradle.libactivity.plugin.result.data.config.LocalConfigCheckResultGroup
import com.mgmtp.gradle.libactivity.plugin.result.data.config.LocalConfigCheckResultGroupMeta

class PlainTextLocalConfigCheckResultFormatter extends AbstractPlainTextCheckResultFormatter<LocalConfigCheckResultGroupMeta, String, LocalConfigCheckResultGroup, LocalConfigCheckResult> {}