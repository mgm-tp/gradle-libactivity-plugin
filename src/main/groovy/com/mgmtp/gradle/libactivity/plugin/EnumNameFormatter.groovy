package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility
import org.apache.commons.text.CaseUtils

@PackageScope
@TupleConstructor
@VisibilityOptions(Visibility.PRIVATE)
final class EnumNameFormatter {

    private static final String WORD_SEPARATOR = '_'

    /**
     * @return Readable enum name as word(s). {@code null} if passed as {@code null}.
     */
    @PackageScope
    static String formatReadable(final Enum<?> anEnum) {

        if (anEnum == null) {
            return null
        }

        return anEnum.name().split(WORD_SEPARATOR)
                .collect { final String wordFromName -> CaseUtils.toCamelCase(wordFromName, true) }
                .join(' ')
    }

    /**
     * @return Enum name in camel-case key notation. {@code null} if passed as {@code null}.
     */
    @PackageScope
    static String formatAsKey(final Enum<?> anEnum) {

        if (anEnum == null) {
            return null
        }

        return CaseUtils.toCamelCase(anEnum.name(), false, WORD_SEPARATOR as char)
    }
}