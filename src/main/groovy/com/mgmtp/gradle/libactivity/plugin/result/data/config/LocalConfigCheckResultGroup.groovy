package com.mgmtp.gradle.libactivity.plugin.result.data.config

import com.mgmtp.gradle.libactivity.plugin.result.data.AbstractCheckResultGroup
import groovy.transform.TupleConstructor

@TupleConstructor(includeSuperProperties = true, callSuper = true)
class LocalConfigCheckResultGroup extends AbstractCheckResultGroup<LocalConfigCheckResultGroupMeta, String> {

    static LocalConfigCheckResultGroup fromGroupMetaAndFindings(LocalConfigCheckResultGroupMeta meta, Collection<String> findings) {
        return new LocalConfigCheckResultGroup(meta, findings)
    }
}