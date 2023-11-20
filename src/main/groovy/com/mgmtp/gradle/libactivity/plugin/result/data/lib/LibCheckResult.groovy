package com.mgmtp.gradle.libactivity.plugin.result.data.lib

import com.mgmtp.gradle.libactivity.plugin.data.lib.Lib
import com.mgmtp.gradle.libactivity.plugin.result.data.AbstractCheckResult
import groovy.transform.TupleConstructor

@TupleConstructor(includeSuperProperties = true, callSuper = true)
class LibCheckResult extends AbstractCheckResult<LibCheckResultGroup> {

    static LibCheckResult fromTaggedLibs(Collection<Lib> taggedLibs) {
        List<LibCheckResultGroup> groups = LibCheckResultGroupMeta.values()
                .collect { LibCheckResultGroupMeta meta -> LibCheckResultGroup.fromGroupMetaAndTaggedLibs(meta, taggedLibs) }
                .findAll { LibCheckResultGroup group -> group.members }
        return new LibCheckResult('LIB ACTIVITY CHECK RESULT', groups)
    }
}