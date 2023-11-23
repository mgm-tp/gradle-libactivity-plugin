package com.mgmtp.gradle.libactivity.plugin.result.format.formatter.json

import com.mgmtp.gradle.libactivity.plugin.data.lib.Lib
import com.mgmtp.gradle.libactivity.plugin.result.data.lib.LibCheckResult
import com.mgmtp.gradle.libactivity.plugin.result.data.lib.LibCheckResultGroup
import groovy.json.JsonBuilder

class JsonLibCheckResultFormatter extends AbstractJsonCheckResultFormatter<LibCheckResult, LibCheckResultGroup> {

    @Override
    JsonBuilder format(LibCheckResult checkResult) {
        return super.format(checkResult)
    }

    @Override
    JsonBuilder getJsonFormattedGroup(LibCheckResultGroup group) {
        return new JsonBuilder(
                category: group.meta.category.name,
                subcategory: group.meta.subCategory?.name,
                members: getJsonFormattedGroupMembers(group).content)
    }

    private static JsonBuilder getJsonFormattedGroupMembers(LibCheckResultGroup group) {
        return new JsonBuilder(group.members.collect { Lib lib -> getJsonFormattedGroupMember(lib, group.meta.detail).content })
    }

    private static JsonBuilder getJsonFormattedGroupMember(Lib member, Lib.Detail groupDetail) {
        Map.Entry<Lib.Detail, ?> matchingDetail = member.details.find { Map.Entry<Lib.Detail, ?> detail -> detail.key == groupDetail }
        return new JsonBuilder(
                mavenIdentifier: [groupId: member.mavenIdentifier.groupId, artifactId: member.mavenIdentifier.artifactId, version: member.mavenIdentifier.version],
                details: matchingDetail ? [(matchingDetail.key.description): matchingDetail.value as String] : null
        )
    }
}