package com.mgmtp.gradle.libactivity.plugin.result.format.formatter.json

import com.mgmtp.gradle.libactivity.plugin.data.lib.Lib
import com.mgmtp.gradle.libactivity.plugin.data.lib.LibDetail
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

    private static JsonBuilder getJsonFormattedGroupMember(Lib member, LibDetail groupDetail) {
        Map.Entry<LibDetail, ?> matchingDetail = member.details.find { Map.Entry<LibDetail, ?> detail -> detail.key == groupDetail }
        return new JsonBuilder(
                coordinates: [groupId: member.coordinates.groupId, artifactId: member.coordinates.artifactId, version: member.coordinates.version],
                details: matchingDetail ? [(matchingDetail.key.description): matchingDetail.value as String] : null
        )
    }
}