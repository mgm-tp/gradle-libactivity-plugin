package com.mgmtp.gradle.libactivity.plugin.result.format.formatter.plaintext

import com.mgmtp.gradle.libactivity.plugin.result.data.CheckResult
import com.mgmtp.gradle.libactivity.plugin.result.data.CheckResultGroup

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
 *
 * @param <GM>
 *     group meta type
 * @param <M>
 *     group member type
 * @param <G>
 *     group type
 * @param <R>
 *     check result type
 */
abstract class AbstractPlainTextCheckResultFormatter<GM extends Enum<GM>, M, G extends CheckResultGroup<GM, M>, R extends CheckResult<GM, M, G>>
        implements PlainTextCheckResultFormatter<GM, M, G, R> {

    @Override
    String format(R checkResult) {
        return checkResult.groups ?
                """\
${getPlainTextCheckResultHeadline(checkResult.name)}


${getPlainTextFormattedGroups(checkResult.groups)}""" : ''
    }

    protected String getPlainTextCheckResultHeadline(String headline) {
        return getPlainTextUnderlinedHeadline(headline, '*')
    }

    protected String getPlainTextGroupHeadline(String headline) {
        return getPlainTextUnderlinedHeadline(headline, '-')
    }

    protected String getPlainTextUnderlinedHeadline(String headline, String underlineChar) {
        return headline + System.lineSeparator() + underlineChar[0] * headline.length()
    }

    protected String getPlainTextFormattedGroups(Collection<G> groups) {
        return groups.sort().collect { G group -> getPlainTextFormattedGroup(group) }.join(System.lineSeparator() * 2)
    }

    protected String getPlainTextFormattedGroup(G group) {
        """\
${getPlainTextGroupHeadline("${group.members.size()}x ${group.meta}")}
${group.members.collect { Object member -> member as String }.sort().join(System.lineSeparator())}"""
    }
}