package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

@PackageScope
@TupleConstructor
@VisibilityOptions(Visibility.PRIVATE)
final class HeadlineFormatter {

    private static final String DEFAULT_UNDERLINE_SYMBOL = '*'

    private static final int DEFAULT_NUM_LINEBREAKS_AFTER_HEADLINE = 2

    /**
     * Formates the passed headline with an underline of asterisk (*) symbols. Adds 2 linebreaks after the underlined
     * headline.
     */
    @PackageScope
    static String format(final String headline) {
        return format(headline, DEFAULT_UNDERLINE_SYMBOL, 2)
    }

    @PackageScope
    static String format(final String headline, final int numLinebreaksAfterHeadline) {
        return format(headline, DEFAULT_UNDERLINE_SYMBOL, numLinebreaksAfterHeadline)
    }

    @PackageScope
    static String format(final String headline, final String underlineSymbol, final int numLinebreaksAfterHeadline) {

        final String oneSizeSymbol = underlineSymbol ? underlineSymbol[0] : DEFAULT_UNDERLINE_SYMBOL
        final int nonNegativeNumLinebreaksAfterHeadline = numLinebreaksAfterHeadline > -1 ? numLinebreaksAfterHeadline : DEFAULT_NUM_LINEBREAKS_AFTER_HEADLINE

        return headline +
                System.lineSeparator() +
                oneSizeSymbol * headline.length() +
                System.lineSeparator() * nonNegativeNumLinebreaksAfterHeadline
    }
}