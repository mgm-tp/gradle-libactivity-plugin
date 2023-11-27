package com.mgmtp.gradle.libactivity.plugin.result.format.formatter.json

import com.mgmtp.gradle.libactivity.plugin.result.data.config.LocalConfigCheckResult
import com.mgmtp.gradle.libactivity.plugin.result.data.config.LocalConfigCheckResultGroup
import com.mgmtp.gradle.libactivity.plugin.result.data.config.LocalConfigCheckResultGroupMeta

class JsonLocalConfigCheckResultFormatter extends AbstractJsonCheckResultFormatter<LocalConfigCheckResultGroupMeta, String, LocalConfigCheckResultGroup, LocalConfigCheckResult> {}