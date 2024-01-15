package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.EqualsAndHashCode
import groovy.transform.PackageScope

@PackageScope
@EqualsAndHashCode
@groovy.transform.NullCheck
class XclusionConfigRedundancyCheckResult implements IConfigRedundancyAware {

    final Set<String> unusedLibXcludes

    final Set<String> unusedLibXcludePatterns

    @PackageScope
    XclusionConfigRedundancyCheckResult(final Collection<String> unusedLibXcludes, final Collection<String> unusedLibXcludePatterns) {
        this.unusedLibXcludes = unusedLibXcludes.asImmutable()
        this.unusedLibXcludePatterns = unusedLibXcludePatterns.asImmutable()
    }

    @PackageScope
    static Builder builder(final XclusionConfig xclusionConfig) {
        return new Builder(xclusionConfig)
    }

    @Override
    boolean hasRedundancy() {
        return unusedLibXcludes || unusedLibXcludePatterns
    }

    @PackageScope
    static class Builder {

        final Set<String> unusedLibXcludes

        final Set<String> unusedLibXcludePatterns

        private Builder(final XclusionConfig xclusionConfig) {
            unusedLibXcludes = new HashSet<>(xclusionConfig.libXcludes)
            unusedLibXcludePatterns = new HashSet<>(xclusionConfig.libXcludePatterns)
        }

        @PackageScope
        Builder usedXclude(final String usedXclude) {
            unusedLibXcludes.remove(usedXclude)
            return this
        }

        @PackageScope
        Builder usedXcludePattern(final String usedXcludePattern) {
            unusedLibXcludePatterns.remove(usedXcludePattern)
            return this
        }

        @PackageScope
        XclusionConfigRedundancyCheckResult build() {
            return new XclusionConfigRedundancyCheckResult(unusedLibXcludes, unusedLibXcludePatterns)
        }
    }
}