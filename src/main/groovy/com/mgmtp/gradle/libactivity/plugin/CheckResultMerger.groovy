package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

import javax.annotation.Nullable

@PackageScope
@TupleConstructor
@VisibilityOptions(Visibility.PRIVATE)
final class CheckResultMerger {

    @PackageScope
    static CheckResultBundle mergeCheckResultBundles(final Collection<CheckResultBundle> checkResultBundles) {
        final LibActivityCheckResult mergedLibActivityCheckResult = mergeLibActivityCheckResultsFromBundles(checkResultBundles)
        final ConfigRedundancyCheckResult mergedConfigRedundancyCheckResult = mergeConfigRedundancyCheckResultsFromBundles(checkResultBundles)
        return new CheckResultBundle(mergedLibActivityCheckResult, mergedConfigRedundancyCheckResult)
    }

    @PackageScope
    static LibActivityCheckResult mergeLibActivityCheckResultsFromBundles(final Collection<CheckResultBundle> checkResultBundles) {

        if (1 == checkResultBundles.size()) {
            return checkResultBundles.first().libActivityCheckResult
        }

        final LibActivityCheckResult.Builder checkResultBuilder = LibActivityCheckResult.builder()

        checkResultBundles
                .collect { final CheckResultBundle checkResultBundle -> checkResultBundle.libActivityCheckResult }
                .each { final LibActivityCheckResult libActivityCheckResult ->
                    checkResultBuilder
                            .mavenId(libActivityCheckResult.mavenId)
                            .detailedApiQueryResults(libActivityCheckResult.detailedApiQueryResults)
                            .cacheData(libActivityCheckResult.cacheData)
                            .fromCache(libActivityCheckResult.fromCache)
                }

        return checkResultBuilder.build()
    }

    @PackageScope
    static ConfigRedundancyCheckResult mergeConfigRedundancyCheckResultsFromBundles(final Collection<CheckResultBundle> checkResultBundles) {

        if (1 == checkResultBundles.size()) {
            return checkResultBundles.first().configRedundancyCheckResult
        }

        final List<ConfigRedundancyCheckResult> configRedundancyCheckResults = checkResultBundles
                .collect { final CheckResultBundle checkResultBundle -> checkResultBundle.configRedundancyCheckResult }

        return mergeConfigRedundancyCheckResults(configRedundancyCheckResults)
    }

    @PackageScope
    static ConfigRedundancyCheckResult mergeConfigRedundancyCheckResults(final Collection<ConfigRedundancyCheckResult> configRedundancyCheckResults) {

        return ConfigRedundancyCheckResult.builder()
                .xclusionConfigRedundancy(mergeXclusionConfigRedundancyCheckResults(configRedundancyCheckResults))
                .gitHubCheckerConfigRedundancy(mergeGitHubCheckerConfigRedundancyCheckResults(configRedundancyCheckResults))
                .build()
    }

    @Nullable
    private static XclusionConfigRedundancyCheckResult mergeXclusionConfigRedundancyCheckResults(
            final Collection<ConfigRedundancyCheckResult> configRedundancyCheckResults) {

        final Set<XclusionConfigRedundancyCheckResult> xclusionConfigRedundancies = configRedundancyCheckResults
                .collect { final ConfigRedundancyCheckResult configRedundancyCheckResult -> configRedundancyCheckResult.xclusionConfigRedundancy }
                .findAll()
                .toSet()

        if (xclusionConfigRedundancies) {

            final List<Set<String>> unusedLibXcludesFromBundles = xclusionConfigRedundancies
                    .collect { final XclusionConfigRedundancyCheckResult xclusionConfigRedundancy -> xclusionConfigRedundancy.unusedLibXcludes }
            final Set<String> mergedUnusedLibXcludes = collectCommonElements(unusedLibXcludesFromBundles)

            final List<Set<String>> unusedLibXcludePatternsFromBundles = xclusionConfigRedundancies
                    .collect { final XclusionConfigRedundancyCheckResult xclusionConfigRedundancy -> xclusionConfigRedundancy.unusedLibXcludePatterns }
            final Set<String> mergedUnusedLibXcludePatterns = collectCommonElements(unusedLibXcludePatternsFromBundles)

            return new XclusionConfigRedundancyCheckResult(mergedUnusedLibXcludes, mergedUnusedLibXcludePatterns)
        }
        // xclusion config redundancy not analyzed at all
        return null
    }

    @Nullable
    private static GitHubLibActivityCheckerConfigRedundancyCheckResult mergeGitHubCheckerConfigRedundancyCheckResults(
            final Collection<ConfigRedundancyCheckResult> configRedundancyCheckResults) {

        final Set<GitHubLibActivityCheckerConfigRedundancyCheckResult> gitHubCheckerConfigRedundancies = configRedundancyCheckResults
                .collect { final ConfigRedundancyCheckResult configRedundancyCheckResult -> configRedundancyCheckResult.gitHubCheckerConfigRedundancy }
                .findAll()
                .toSet()

        if (gitHubCheckerConfigRedundancies) {

            final List<Set<String>> unusedGitHubMappingKeysFromBundles = gitHubCheckerConfigRedundancies
                    .collect { final GitHubLibActivityCheckerConfigRedundancyCheckResult gitHubCheckerConfigRedundancy ->
                        gitHubCheckerConfigRedundancy.unusedLocalGitHubMappingKeys
                    }
            final Set<String> mergedUnusedGitHubMappingKeys = collectCommonElements(unusedGitHubMappingKeysFromBundles)

            return new GitHubLibActivityCheckerConfigRedundancyCheckResult(mergedUnusedGitHubMappingKeys)
        }
        // GitHub checker config redundancy not analyzed at all
        return null
    }

    private static <T> Set<T> collectCommonElements(final Collection<? extends Collection<T>> collectionOfCollections) {

        if (collectionOfCollections) {
            return collectionOfCollections
                    .inject { final Collection<String> commonElements, final Collection<String> nextElements -> commonElements.intersect(nextElements) }
                    .toSet()
        }

        return Set.of()
    }

    @PackageScope
    static ConfigRedundancyCheckResult mergeConfigRedundancyCheckResultWithBundledResults(
            final ConfigRedundancyCheckResult configRedundancyCheckResult, final Collection<CheckResultBundle> checkResultBundles) {

        if (checkResultBundles) {

            final Collection<ConfigRedundancyCheckResult> configRedundancyCheckResults = checkResultBundles
                    .collect { final CheckResultBundle checkResultBundle -> checkResultBundle.configRedundancyCheckResult }
            configRedundancyCheckResults << configRedundancyCheckResult

            return mergeConfigRedundancyCheckResults(configRedundancyCheckResults)
        }

        return configRedundancyCheckResult
    }
}