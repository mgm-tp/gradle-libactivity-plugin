package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope
import org.gradle.api.Project

@PackageScope
class LibActivityCheckManager {

    private final Config config

    private final LibCollector libCollector

    private final LibActivityCheckResultCacheManager cacheManager

    private static final LazyLogger LOGGER = LazyLogger.fromClazz(LibActivityCheckManager.class)

    @PackageScope
    static LibActivityCheckManager fromConfig(final Config localConfig) {
        return new LibActivityCheckManager(localConfig)
    }

    @groovy.transform.NullCheck
    private LibActivityCheckManager(final Config config) {
        this.config = config
        libCollector = LibCollector.fromConfig(config)
        cacheManager = LibActivityCheckResultCacheManager.fromConfig(config)
    }

    /**
     * @param rootProject The project to check.
     */
    @PackageScope
    void checkProject(final Project rootProject) {

        LOGGER.info("Start lib check for project '${rootProject.name}'.")

        final LibCollector.ResultBundle libCollectorResultBundle = libCollector.collectExternalLibsFromProjectAndSubProjects(rootProject)
        final Set<MavenId> mavenIds = libCollectorResultBundle.mavenIds

        validateLibMavenIds(mavenIds)

        checkMavenIdsCacheAware(mavenIds, libCollectorResultBundle.configRedundancyCheckResult)
    }

    private static void validateLibMavenIds(final Collection<MavenId> libMavenIds) {

        LOGGER.info('Validate lib Maven IDs.')

        if (!libMavenIds) {
            throw new IllegalStateException('Received no lib Maven IDs to check')
        }
    }

    private void checkMavenIdsCacheAware(final Collection<MavenId> mavenIds, final ConfigRedundancyCheckResult configRedundancyCheckResult) {

        try {

            final Set<CheckResultBundle> cachedCheckResultBundles = cacheManager.getUpdatedCachedCheckResults(mavenIds)
            final ConfigRedundancyCheckResult mergedConfigRedundancyCheckResult = CheckResultMerger
                    .mergeConfigRedundancyCheckResultWithBundledResults(configRedundancyCheckResult, cachedCheckResultBundles)
            final Set<LibActivityCheckResult> cachedLibActivityCheckResults = getLibActivityCheckResultsFromBundles(cachedCheckResultBundles)
            final Set<MavenId> mavenIdsToCheck = getMavenIdsToCheck(mavenIds, cachedLibActivityCheckResults)

            checkMavenIdsCacheAware(mavenIdsToCheck, cachedLibActivityCheckResults, mergedConfigRedundancyCheckResult)
        }
        finally {
            cacheManager.shutdown()
        }
    }

    private void checkMavenIdsCacheAware(
            final Collection<MavenId> mavenIdsToCheck,
            final Collection<LibActivityCheckResult> cachedLibActivityCheckResults,
            final ConfigRedundancyCheckResult configRedundancyCheckResult) {

        final Set<LibActivityCheckResult> newLibActivityCheckResults = new HashSet<>()

        try {

            final Set<ConfigRedundancyCheckResult> configRedundancyCheckResults = new HashSet<>()

            checkMavenIdsAndFillResults(mavenIdsToCheck, newLibActivityCheckResults, configRedundancyCheckResults)

            final Collection<LibActivityCheckResult> allLibActivityCheckResults = cachedLibActivityCheckResults + newLibActivityCheckResults
            formatAndWriteLibActivityCheckResults(allLibActivityCheckResults)

            final ConfigRedundancyCheckResult mergedConfigRedundancyCheckResult = CheckResultMerger
                    .mergeConfigRedundancyCheckResults(configRedundancyCheckResults + configRedundancyCheckResult)
            formatAndWriteConfigRedundancyCheckResult(mergedConfigRedundancyCheckResult)

            failTaskIfRequired(allLibActivityCheckResults)
        } finally {
            cacheManager.cacheCheckResults(newLibActivityCheckResults)
        }
    }

    private void checkMavenIdsAndFillResults(
            final Collection<MavenId> mavenIds,
            final Collection<LibActivityCheckResult> libActivityCheckResultsModifiable,
            final Collection<ConfigRedundancyCheckResult> configRedundancyCheckResultsModifiable) {

        LOGGER.info { "Check activity for ${mavenIds.size()} libs via APIs ${config.apis}." }

        for (final MavenId mavenId : mavenIds) {
            CheckResultBundle checkResultBundle = checkMavenId(mavenId)
            libActivityCheckResultsModifiable << checkResultBundle.libActivityCheckResult
            configRedundancyCheckResultsModifiable << checkResultBundle.configRedundancyCheckResult
        }
    }

    /**
     * ?
     */
    private CheckResultBundle checkMavenId(final MavenId mavenId) {

        final Set<CheckResultBundle> apiCheckResultBundles = new HashSet<>()

        for (final Api api : config.apis) {
            // if creation of individual checkers will someday become more expensive then we should reuse instances
            CheckResultBundle apiCheckResultBundle = ApiLibActivityCheckerFactory.getChecker(api, config).check(mavenId)
            apiCheckResultBundles << apiCheckResultBundle
            // quit early in case of activity to save time and possibly request contingent
            if (LibActivityCheckResult.Category.ACTIVE == apiCheckResultBundle.libActivityCheckResult.category) {
                break
            }
        }

        final LibActivityCheckResult mergedLibActivityCheckResult = CheckResultMerger.mergeLibActivityCheckResultsFromBundles(apiCheckResultBundles)
        final ConfigRedundancyCheckResult mergedConfigRedundancyCheckResult = CheckResultMerger.mergeConfigRedundancyCheckResultsFromBundles(apiCheckResultBundles)

        return new CheckResultBundle(mergedLibActivityCheckResult, mergedConfigRedundancyCheckResult)
    }

    private static Set<LibActivityCheckResult> getLibActivityCheckResultsFromBundles(final Collection<CheckResultBundle> checkResultBundles) {
        return checkResultBundles.collect { final CheckResultBundle checkResultBundle -> checkResultBundle.libActivityCheckResult }.toSet()
    }

    private static Set<MavenId> getMavenIdsToCheck(final Collection<MavenId> allMavenIds, final Collection<LibActivityCheckResult> cachedCheckResults) {
        return allMavenIds - cachedCheckResults.collect { final LibActivityCheckResult checkResult -> checkResult.mavenId }
    }

    private void formatAndWriteLibActivityCheckResults(final Collection<LibActivityCheckResult> checkResults) {

        if (checkResults) {
            LOGGER.info('Format lib activity check results.')
            final String formattedLibActivityCheckResults = LibActivityCheckResultFormatterFactory.getFormatter(config).format(checkResults)
            writeFormattedCheckResult(formattedLibActivityCheckResults, config.checkResultReportConfig.libActivityCheckResultFile)
        } else {
            LOGGER.info('Empty collection of lib activity check results. Skip write.')
        }
    }

    private void formatAndWriteConfigRedundancyCheckResult(final ConfigRedundancyCheckResult configRedundancyCheckResult) {

        if (configRedundancyCheckResult.hasRedundancy()) {

            LOGGER.info('Format redundant local config check result.')
            final String formattedConfigRedundancyCheckResult = ConfigRedundancyCheckResultFormatterFactory
                    .getFormatter(config.checkResultReportConfig.format)
                    .format(configRedundancyCheckResult)

            writeFormattedCheckResult(formattedConfigRedundancyCheckResult, config.checkResultReportConfig.configRedundancyCheckResultFile)
        } else {
            LOGGER.info('Empty config redundancy check result. Skip write.')
        }
    }

    private void writeFormattedCheckResult(final String formattedCheckResult, final File targetFile) {

        if (OutputChannel.FILE in config.checkResultReportConfig.channels) {
            LOGGER.info { "Write result to file: ${targetFile}" }
            new FileWriter(targetFile).write(formattedCheckResult)
        }
        if (OutputChannel.CONSOLE in config.checkResultReportConfig.channels) {
            LOGGER.info { 'Write result to console.' }
            new ConsoleWriter().write(formattedCheckResult)
        }
    }

    private void failTaskIfRequired(final Collection<LibActivityCheckResult> checkResults) {

        final List<LibActivityCheckResult.Category> criticalCategories = checkResults
                .collect { final LibActivityCheckResult checkResult -> checkResult.category }
                .findAll { final LibActivityCheckResult.Category category -> category in config.failAtLibActivityCheckResultCategories }
                .toSet()
                .sort()

        if (criticalCategories) {
            throw new IllegalStateException("Found categories in lib activity check results that require task to fail: ${criticalCategories}. See report for details.")
        }
    }
}