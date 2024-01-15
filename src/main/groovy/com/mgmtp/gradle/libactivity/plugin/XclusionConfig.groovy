package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

import javax.annotation.Nullable

@PackageScope
@TupleConstructor
@VisibilityOptions(Visibility.PRIVATE)
class XclusionConfig {

    final Set<String> libXcludes

    final Set<String> libXcludePatterns

    @Override
    String toString() {
        return "${libXcludes.size()}x libXcludes; ${libXcludePatterns.size()}x libXcludePatterns"
    }

    @PackageScope
    @groovy.transform.NullCheck
    static XclusionConfig fromExtension(final XclusionExtension extension) {

        NullCheck.collection(extension.libXcludes)
        NullCheck.collection(extension.libXcludePatterns)

        final Set<String> trimmedLibXcludes = initXclusions(extension.libXcludes)
        final Set<String> trimmedLibXcludePatterns = initXclusions(extension.libXcludePatterns)

        validateXclusions(trimmedLibXcludes, 'libXcludes')
        validateXclusions(trimmedLibXcludePatterns, 'libXcludePatterns')

        return new XclusionConfig(trimmedLibXcludes, trimmedLibXcludePatterns)
    }

    private static void validateXclusions(final Collection<String> xclusions, final String name) {

        final boolean containsEmptyElement = xclusions.any { final String xclusion -> !xclusion }

        if (containsEmptyElement) {
            throw new IllegalStateException("Trimmed '${name}' must not contain empty elements")
        }
    }

    private static Set<String> initXclusions(@Nullable final Collection<String> xclusions) {
        return xclusions ? xclusions.collect { final String xclusion -> xclusion.trim() }.asImmutable() : Set.of()
    }
}