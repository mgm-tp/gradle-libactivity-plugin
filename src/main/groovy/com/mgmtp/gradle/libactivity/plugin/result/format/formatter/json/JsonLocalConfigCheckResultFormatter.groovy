package com.mgmtp.gradle.libactivity.plugin.result.format.formatter.json

import com.mgmtp.gradle.libactivity.plugin.result.data.config.LocalConfigCheckResult
import com.mgmtp.gradle.libactivity.plugin.result.data.config.LocalConfigCheckResultGroup
import groovy.json.JsonBuilder

class JsonLocalConfigCheckResultFormatter extends AbstractJsonCheckResultFormatter<LocalConfigCheckResult, LocalConfigCheckResultGroup> {

    @Override
    JsonBuilder format(LocalConfigCheckResult checkResult) {
        return super.format(checkResult)
    }
}