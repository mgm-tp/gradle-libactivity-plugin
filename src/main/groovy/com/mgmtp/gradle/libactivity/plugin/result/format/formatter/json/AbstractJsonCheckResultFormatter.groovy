package com.mgmtp.gradle.libactivity.plugin.result.format.formatter.json

import com.mgmtp.gradle.libactivity.plugin.result.data.CheckResult
import com.mgmtp.gradle.libactivity.plugin.result.data.CheckResultGroup
import groovy.json.JsonBuilder

/**
 * @param <GM>
 *     group meta type
 * @param <M>
 *     group member type
 * @param <G>
 *     group type
 * @param <R>
 *     check result type
 */
abstract class AbstractJsonCheckResultFormatter<GM extends Enum<GM>, M, G extends CheckResultGroup<GM, M>, R extends CheckResult<GM, M, G>>
        implements JsonCheckResultFormatter<GM, M, G, R> {

    @Override
    JsonBuilder format(R checkResult) {
        if (getJsonFormattedCheckResultGroups(checkResult.groups).content) {
            return new JsonBuilder(name: checkResult.name, groups: getJsonFormattedCheckResultGroups(checkResult.groups).content)
        }
        return new JsonBuilder()
    }

    protected JsonBuilder getJsonFormattedCheckResultGroups(Collection<G> groups) {
        return groups ? new JsonBuilder(groups.sort().collect { G group -> getJsonFormattedGroup(group).content }) : new JsonBuilder()
    }

    protected JsonBuilder getJsonFormattedGroup(G group) {
        return new JsonBuilder(name: group.meta as String, members: group.members.sort())
    }
}