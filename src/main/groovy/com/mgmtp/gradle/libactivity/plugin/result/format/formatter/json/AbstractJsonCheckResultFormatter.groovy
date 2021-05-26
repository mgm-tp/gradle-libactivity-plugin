package com.mgmtp.gradle.libactivity.plugin.result.format.formatter.json

import com.mgmtp.gradle.libactivity.plugin.result.data.AbstractCheckResult
import com.mgmtp.gradle.libactivity.plugin.result.data.AbstractCheckResultGroup
import groovy.json.JsonBuilder

abstract class AbstractJsonCheckResultFormatter<R extends AbstractCheckResult,G extends AbstractCheckResultGroup> implements JsonCheckResultFormatter<R> {

    @Override
    JsonBuilder format( R checkResult) {
        if( getJsonFormattedCheckResultGroups( checkResult.groups).content) {
            return new JsonBuilder( name: checkResult.name, groups: getJsonFormattedCheckResultGroups( checkResult.groups).content)
        }
        return new JsonBuilder( )
    }

    protected JsonBuilder getJsonFormattedCheckResultGroups( Collection<G> groups) {
        return groups ? new JsonBuilder( groups.sort( ).collect{ G group -> getJsonFormattedGroup( group).content}) : new JsonBuilder( )
    }

    protected JsonBuilder getJsonFormattedGroup( G group) {
        return new JsonBuilder( name: group.meta as String, members: group.members.sort( ))
    }
}