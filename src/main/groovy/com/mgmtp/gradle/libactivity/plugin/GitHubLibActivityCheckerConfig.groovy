package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility
import org.apache.commons.lang3.StringUtils

import javax.annotation.Nullable

@TupleConstructor
@VisibilityOptions(Visibility.PRIVATE)
class GitHubLibActivityCheckerConfig implements ILibActivityCheckerConfig {

    private static final LazyLogger LOGGER = LazyLogger.fromClazz(GitHubLibActivityCheckerConfig.class)

    final Map<String, String> localGitHubMappings

    @Nullable
    final String personalAccessToken

    // we pull these mappings lazy => if GitHub is configured as subordinate API it may not be queried at all
    private Map<String, String> globalGitHubMappings

    @groovy.transform.NullCheck
    static GitHubLibActivityCheckerConfig fromExtension(final GitHubLibActivityCheckerExtension extension) {

        NullCheck.map(extension.localGitHubMappings)

        final Map<String,String> trimmedLocalGitHubMappings = initLocalGitHubMappings(extension.localGitHubMappings)

        validateTrimmedLocalGitHubMappingKeys(trimmedLocalGitHubMappings.keySet())

        return new GitHubLibActivityCheckerConfig(trimmedLocalGitHubMappings, StringUtils.trim(extension.personalAccessToken))
    }

    private static validateTrimmedLocalGitHubMappingKeys(final Set<String> trimmedLocalGitHubMappingKeys) {

        final boolean containsEmptyKey = trimmedLocalGitHubMappingKeys.any { final String key -> !key}

        if(containsEmptyKey) {
            throw new IllegalStateException('Trimmed local GitHub mapping keys must not be empty')
        }
    }

    /**
     * @return The global GitHub mappings. If not yet present they will be loaded from plugin classpath.
     */
    @PackageScope
    Map<String, String> getGlobalGitHubMappings() {

        if (!globalGitHubMappings) {
            globalGitHubMappings = initGlobalGitHubMappings()
        }
        return globalGitHubMappings
    }

    @Override
    String toString() {
        return '{' + getClass().simpleName + ': localGitHubMappings: ' + localGitHubMappings.size() + 'x ; personalAccessToken: ' + (personalAccessToken ? 'YES' : 'NO') + '}'
    }

    private static Map<String, String> initGlobalGitHubMappings() {

        final Properties properties = new Properties()

        LOGGER.info('Load global GitHub mapping properties from classpath.')

        try {
            properties.load(GitHubLibActivityCheckerConfig.class.getResourceAsStream('/github/gitHubMapping.properties'))
        } catch (final Exception e) {
            LOGGER.warn('Read global GitHub mapping properties caused error.', e)
        }

        return properties
                .collectEntries { final Object key, final Object value -> [(key as String): value as String] }
                .collectEntries { final String key, final String value -> [(key.trim()): value.trim()] }
    }

    private static Map<String, String> initLocalGitHubMappings(final Map<String, String> localGitHubMappings) {
            return localGitHubMappings
                    .collectEntries { final String key, final String value -> [(key.trim()): value.trim()] }
                    .asImmutable()
    }
}