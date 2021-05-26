package com.mgmtp.gradle.libactivity.plugin.result.format.formatter.plaintext

import com.mgmtp.gradle.libactivity.plugin.result.data.AbstractCheckResult
import com.mgmtp.gradle.libactivity.plugin.result.data.AbstractCheckResultGroup

/**
 * Basic plaintext format with group members that can be represented merely by {@link Object#toString}.
 *
 * check result headline
 * *********************
 *
 *
 * group 1 headline
 * ----------------
 * member 1
 * .
 * .
 * member N
 *
 * group 2 headline
 * ----------------
 * .
 * .
 */
abstract class AbstractPlainTextCheckResultFormatter<R extends AbstractCheckResult, G extends AbstractCheckResultGroup> implements PlainTextCheckResultFormatter<R> {

    @Override
    String format( R checkResult) {
        return checkResult.groups ?
"""\
${ getPlainTextCheckResultHeadline( checkResult.name)}


${ getPlainTextFormattedGroups( checkResult.groups)}""" : ''
    }

    protected String getPlainTextCheckResultHeadline( String headline) {
        return getPlainTextUnderlinedHeadline( headline, '*')
    }

    protected String getPlainTextGroupHeadline( String headline) {
        return getPlainTextUnderlinedHeadline( headline, '-')
    }

    protected String getPlainTextUnderlinedHeadline( String headline, String underlineChar) {
        return headline + System.lineSeparator( ) + underlineChar[0] * headline.length( )
    }

    protected String getPlainTextFormattedGroups( Collection<G> groups) {
        return groups.sort( ).collect { G group -> getPlainTextFormattedGroup( group)}.join( System.lineSeparator( ) * 2)
    }

    protected String getPlainTextFormattedGroup( G group) {
"""\
${ getPlainTextGroupHeadline( "${group.members.size( )}x ${ group.meta}")}
${ group.members.collect{ Object member -> member as String}.sort( ).join( System.lineSeparator( ))}"""
    }
}