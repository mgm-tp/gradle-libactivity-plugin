package com.mgmtp.gradle.libactivity.plugin.result.format.formatter.plaintext

import com.mgmtp.gradle.libactivity.plugin.data.lib.Lib
import com.mgmtp.gradle.libactivity.plugin.data.lib.LibDetail
import com.mgmtp.gradle.libactivity.plugin.result.data.lib.LibCheckResult
import com.mgmtp.gradle.libactivity.plugin.result.data.lib.LibCheckResultGroup

class PlainTextLibCheckResultFormatter extends AbstractPlainTextCheckResultFormatter<LibCheckResult,LibCheckResultGroup> {

    @Override
    String format( LibCheckResult checkResult) {
        return checkResult.groups ?
"""\
${ getPlainTextCheckResultHeadline( checkResult.name)}

(<Detail>) marks detail, e.g.: (number of commits) = number of commits found on GitHub
       (*) marks group with libraries that can be found in other groups as well


${ getPlainTextFormattedGroups( checkResult.groups)}""" : ''
    }

    @Override
    protected String getPlainTextFormattedGroup( LibCheckResultGroup group) {
        String detailSchema = group.meta.detail ? " ---> (${ group.meta.detail.description})" : ''
        String noUniqueMembersSymbol = group.meta.isGroupWithUniqueMembers ? '' : ' (*)'
        String plainTextGroupName = "${ group.members.size( )}x ${ group.meta}${ noUniqueMembersSymbol}${ detailSchema}"
"""\
${ getPlainTextGroupHeadline( plainTextGroupName)}
${ group.members.sort( ).collect { Lib lib -> lib.toString( ) + getDetailValueFromLib( lib, group.meta.detail)}.join( System.lineSeparator( ))}"""
    }

    private static String getDetailValueFromLib(Lib lib, LibDetail groupDetail) {
        String detailValue = lib.details.find { Map.Entry<LibDetail,?> libDetail -> libDetail.key == groupDetail}?.value
        return detailValue ? " (${ detailValue})" : ''
    }
}