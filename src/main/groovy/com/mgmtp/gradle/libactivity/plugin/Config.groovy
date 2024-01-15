package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope
import groovy.transform.builder.Builder
import org.gradle.api.Project

/**
 * Contains properties that can be set by the user when configuring the {@link CheckLibActivityTask} task locally in a project.
 */
@PackageScope
class Config {

    final String rootProjectName

    final ActivityTimeframeConfig activityTimeframeConfig

    final boolean checkBuildscript

    final List<Api> apis

    final XclusionConfig xclusionConfig

    final Set<ILibActivityCheckerConfig> libActivityCheckerConfigs

    final Set<LibActivityCheckResult.Category> failAtLibActivityCheckResultCategories

    final CheckResultReportConfig checkResultReportConfig

    final CheckResultCacheConfig checkResultCacheConfig

    final HttpClient httpClient

    private static final LazyLogger LOGGER = LazyLogger.fromClazz(Config.class)

    @Builder
    private Config(
            final Project rootProject,
            final long referenceTimestampEpochMilli,
            final int maxDaysSinceLatestActivity,
            final boolean checkBuildscript,
            final List<Api> apis,
            final HttpClient httpClient,
            final Set<LibActivityCheckResult.Category> failAtLibActivityCheckResultCategories) {

        LOGGER.info('Validate local config.')

        Objects.requireNonNull(rootProject)
        validateApis(apis)
        NullCheck.collection(failAtLibActivityCheckResultCategories)

        verifyNumericParamIsPositive(apis.size(), 'apis')

        LOGGER.info('Initialize local config.')

        this.rootProjectName = rootProject.name
        activityTimeframeConfig = ActivityTimeframeConfig.builder()
                .maxDaysSinceLatestActivity(maxDaysSinceLatestActivity)
                .referenceTimestampEpochMilli(referenceTimestampEpochMilli)
                .build()
        this.checkBuildscript = checkBuildscript
        this.apis = apis.asImmutable()
        this.httpClient = httpClient ?: new HttpClient()
        xclusionConfig = XclusionConfig.fromExtension(rootProject.extensions.getByType(XclusionExtension.class))
        libActivityCheckerConfigs = initLibActivityCheckerConfig(apis, rootProject)
        this.failAtLibActivityCheckResultCategories = EnumSet.copyOf(failAtLibActivityCheckResultCategories).asImmutable()

        checkResultReportConfig = CheckResultReportConfig.fromProject(rootProject)
        checkResultCacheConfig = CheckResultCacheConfig.fromProject(rootProject)

        LOGGER.info { "Local config complete: ${this}." }
    }

    private static void validateApis(final List<Api> apis) {

        NullCheck.collection(apis)

        final Set<Api> duplicateApis = apis
                .countBy { final Api api -> api }
                .findAll { final Api api, final Integer times -> times > 1 }
                .keySet()

        if (duplicateApis) {
            throw new IllegalStateException("List of APIs must contain unique elements. Found duplicates: ${duplicateApis}")
        }
    }

    @PackageScope
    <T extends ILibActivityCheckerConfig> T getLibActivityCheckerConfig(final Class<T> checkerConfigClazz) {

        final ILibActivityCheckerConfig checkerConfig = libActivityCheckerConfigs
                .find { final ILibActivityCheckerConfig libActivityCheckerConfig -> libActivityCheckerConfig.class == checkerConfigClazz }

        if (!checkerConfig) {
            throw new IllegalArgumentException("No lib activity checker config registered for type ${checkerConfigClazz.name}")
        }

        return (T) checkerConfig
    }

    @Override
    String toString() {
        """
rootProjectName = ${rootProjectName}
activityTimeframeConfig = ${activityTimeframeConfig}
checkBuildscript = ${checkBuildscript}
apis = ${apis}
xclusionConfig = ${xclusionConfig}
libActivityCheckerConfigs = ${libActivityCheckerConfigs.toList()}
failAtLibActivityCheckResultCategories = ${failAtLibActivityCheckResultCategories}
checkResultReportConfig = ${checkResultReportConfig}
checkResultCacheConfig = ${checkResultCacheConfig}"""
    }

    private static void verifyNumericParamIsPositive(final int num, final String paramName) {
        if (num < 1) {
            throw new IllegalArgumentException("${paramName} must be positive. Received: ${num}.")
        }
    }

    /**
     * For consistency we go through the current APIs and register a matching checker config if there is one.
     */
    private static Set<ILibActivityCheckerConfig> initLibActivityCheckerConfig(final List<Api> apis, final Project rootProject) {

        return apis.collect(new ArrayList<>()) { final Api api ->

            switch (api) {
                case Api.GITHUB:
                    return GitHubLibActivityCheckerConfig.fromExtension(rootProject.extensions.getByType(GitHubLibActivityCheckerExtension.class))
                default:
                    return null
            }
        }.findAll().toSet().asImmutable()
    }
}