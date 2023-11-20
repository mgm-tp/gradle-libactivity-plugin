package com.mgmtp.gradle.libactivity.plugin.result.data.config

import com.mgmtp.gradle.libactivity.plugin.result.data.AbstractCheckResult
import groovy.transform.TupleConstructor

@TupleConstructor(includeSuperProperties = true, callSuper = true)
class LocalConfigCheckResult extends AbstractCheckResult<LocalConfigCheckResultGroup> {

    static LocalConfigCheckResult fromGroups(Collection<LocalConfigCheckResultGroup> groups) {
        return new LocalConfigCheckResult('REDUNDANT LOCAL CONFIG ENTRIES', groups)
    }
}