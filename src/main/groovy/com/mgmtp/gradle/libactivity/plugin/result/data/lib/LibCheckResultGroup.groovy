package com.mgmtp.gradle.libactivity.plugin.result.data.lib

import com.mgmtp.gradle.libactivity.plugin.data.lib.Lib
import com.mgmtp.gradle.libactivity.plugin.result.data.AbstractCheckResultGroup
import groovy.transform.TupleConstructor

@TupleConstructor(includeSuperProperties = true, callSuper = true)
class LibCheckResultGroup extends AbstractCheckResultGroup<LibCheckResultGroupMeta, Lib> {

    static LibCheckResultGroup fromGroupMetaAndTaggedLibs(LibCheckResultGroupMeta meta, Collection<Lib> taggedLibs) {
        return new LibCheckResultGroup(meta, taggedLibs.findAll { Lib taggedLib -> taggedLib.tags.containsAll(meta.libTags) })
    }
}