package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility
import org.gradle.api.Project
import org.gradle.api.artifacts.*
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependencyConstraint

@PackageScope
@TupleConstructor(post = { NullCheck.ALL_PROPS.call(this) })
@VisibilityOptions(Visibility.PRIVATE)
class LibCollector {

    private static final LazyLogger LOGGER = LazyLogger.fromClazz(LibCollector.class)

    final Config config

    @PackageScope
    static LibCollector fromConfig(final Config config) {
        return new LibCollector(config)
    }

    /**
     * <p>
     * Collects dependencies + constraints from resolvable and non-resolvable configurations and turns them into Maven IDs.
     * This gives us both dependencies consumed by the project and those provided by the project as consumables to others.
     * </p>
     * <p>
     * Only external dependencies and constraints are taken into account (project dependencies will not be collected). We
     * assume that the user has influence on such internal dependencies and their active development.
     * </p>
     *
     * @return Result bundle with unique Maven IDs of collected libs plus config redundancy check result for lib exclusions.
     */
    @PackageScope
    ResultBundle collectExternalLibsFromProjectAndSubProjects(final Project rootProject) {

        LOGGER.info { "Collect external libs from direct ${config.checkBuildscript ? 'and buildscript' : ''} configurations in root project ${rootProject.name} and sub-projects." }

        final XclusionConfigRedundancyCheckResult.Builder xclusionConfigRedundancyBuilder = XclusionConfigRedundancyCheckResult.builder(config.xclusionConfig)
        final Set<ConfigurationContainer> configurationContainers = collectConfigurationContainersFromProjectAndSubProjects(rootProject)
        final Set<MavenId> mavenIds = collectExternalLibsFromConfigurationContainers(configurationContainers, xclusionConfigRedundancyBuilder)

        LOGGER.info { "${mavenIds.size()} libs collected." }

        final ConfigRedundancyCheckResult configRedundancyCheckResult = ConfigRedundancyCheckResult.builder()
                .xclusionConfigRedundancy(xclusionConfigRedundancyBuilder.build())
                .build()

        return new ResultBundle(mavenIds, configRedundancyCheckResult)
    }

    private Set<ConfigurationContainer> collectConfigurationContainersFromProjectAndSubProjects(final Project rootProject) {
        final Set<ConfigurationContainer> configurationContainers = new HashSet<>()
        rootProject.allprojects.each { final Project project -> collectConfigurationContainersFromProject(project, configurationContainers) }
        return configurationContainers
    }

    // an "add-to-prepared-collection" variant to collect containers since "flatten()" works recursively and unpacks the containers themselves
    private void collectConfigurationContainersFromProject(final Project project, final Collection<ConfigurationContainer> configurationContainersModifiable) {

        if (config.checkBuildscript) {
            configurationContainersModifiable << project.buildscript.configurations
        }
        configurationContainersModifiable << project.configurations
    }

    private Set<MavenId> collectExternalLibsFromConfigurationContainers(
            final Collection<ConfigurationContainer> configurationContainers,
            final XclusionConfigRedundancyCheckResult.Builder xclusionConfigRedundancyBuilder) {

        return configurationContainers
                .collect { final ConfigurationContainer configurationContainer -> configurationContainer.asMap.values() }
                .flatten()
                .collect { final Object object -> (Configuration) object }
                .collect { final Configuration configuration -> collectExternalLibsFromConfiguration(configuration, xclusionConfigRedundancyBuilder) }
                .flatten()
                .collect { final Object object -> (MavenId) object }
                .toSet()
    }

    private Set<MavenId> collectExternalLibsFromConfiguration(
            final Configuration configuration, final XclusionConfigRedundancyCheckResult.Builder xclusionConfigRedundancyBuilder) {

        final Set<MavenId> externalDependencies = collectExternalDependencies(configuration, xclusionConfigRedundancyBuilder)
        final Set<MavenId> externalDependencyConstraints = collectExternalDependencyConstraints(configuration, xclusionConfigRedundancyBuilder)

        return externalDependencies + externalDependencyConstraints
    }

    private Set<MavenId> collectExternalDependencies(
            final Configuration configuration, final XclusionConfigRedundancyCheckResult.Builder xclusionConfigRedundancyBuilder) {

        LOGGER.debug { "Collect external dependencies from configuration '${configuration.name}'" }

        return configuration.dependencies
                .findAll { final Dependency dependency -> !(dependency instanceof ProjectDependency) }
        // group may be null which is not permitted in Maven ID
                .findAll { final Dependency dependency -> dependency.group }
                .findAll { final Dependency dependency -> isNoXcludedLib(dependency.group, dependency.name, xclusionConfigRedundancyBuilder) }
                .collect { final Dependency dependency ->
                    LOGGER.debug { "Collect dependency '${dependency.group}:${dependency.name}:${dependency.version};${configuration.name}'" }
                    new MavenId(dependency.group, dependency.name)
                }
                .toSet()
    }

    private Set<MavenId> collectExternalDependencyConstraints(
            final Configuration configuration, final XclusionConfigRedundancyCheckResult.Builder xclusionConfigRedundancyBuilder) {

        LOGGER.debug { "Collect external dependency constraints from configuration '${configuration.name}'" }

        return configuration.dependencyConstraints
                .findAll { final DependencyConstraint constraint -> !(constraint instanceof DefaultProjectDependencyConstraint) }
                .findAll { final DependencyConstraint constraint -> isNoXcludedLib(constraint.group, constraint.name, xclusionConfigRedundancyBuilder) }
                .collect { final DependencyConstraint constraint ->
                    LOGGER.debug { "Collect dependency constraint '${constraint.module}:${constraint.versionConstraint};${configuration.name}'" }
                    new MavenId(constraint.group, constraint.name)
                }
                .toSet()
    }

    /** xcludes are check prior to xcludePatterns. */
    private boolean isNoXcludedLib(
            final String groupId, final String artifactId, final XclusionConfigRedundancyCheckResult.Builder xclusionConfigRedundancyBuilder) {

        final String groupArtifactTuple = groupId + ':' + artifactId
        LOGGER.debug { "Verify lib '${groupArtifactTuple}' is not excluded from check" }
        return groupArtifactTupleMatchesNoXclude(groupArtifactTuple, xclusionConfigRedundancyBuilder) &&
                groupArtifactTupleMatchesNoXcludePattern(groupArtifactTuple, xclusionConfigRedundancyBuilder)
    }

    private boolean groupArtifactTupleMatchesNoXclude(
            final String groupArtifactTuple, final XclusionConfigRedundancyCheckResult.Builder xclusionConfigRedundancyBuilder) {

        final String matchedXcludeItem = config.xclusionConfig.libXcludes
                .find { final String xclude -> groupArtifactTupleMatchesXclude(groupArtifactTuple, xclude) }

        if (matchedXcludeItem) {
            xclusionConfigRedundancyBuilder.usedXclude(matchedXcludeItem)
        }

        return !matchedXcludeItem
    }

    private boolean groupArtifactTupleMatchesNoXcludePattern(
            final String groupArtifactTuple, final XclusionConfigRedundancyCheckResult.Builder xclusionConfigRedundancyBuilder) {

        final String matchedXcludePattern = config.xclusionConfig.libXcludePatterns
                .find { final String xcludePattern -> groupArtifactTupleMatchesXcludePattern(groupArtifactTuple, xcludePattern) }

        if (matchedXcludePattern) {
            xclusionConfigRedundancyBuilder.usedXcludePattern(matchedXcludePattern)
        }

        return !matchedXcludePattern
    }

    private static boolean groupArtifactTupleMatchesXclude(final String groupArtifactTuple, final String xclude) {
        LOGGER.debug('Check group:artifact \'{}\' against xclude \'{}\'.', groupArtifactTuple, xclude)
        return xclude == groupArtifactTuple
    }

    private static boolean groupArtifactTupleMatchesXcludePattern(final String groupArtifactTuple, final String xcludePattern) {
        LOGGER.debug('Check group:artifact \'{}\' against xclude pattern \'{}\'.', groupArtifactTuple, xcludePattern)
        groupArtifactTuple.matches(xcludePattern)
    }

    @PackageScope
    @TupleConstructor(post = { NullCheck.ALL_PROPS.call(this) })
    @VisibilityOptions(Visibility.PRIVATE)
    static class ResultBundle {

        final Set<MavenId> mavenIds

        // though an xclusion config usage would suffice at the moment we may need to provide more usages in the future
        final ConfigRedundancyCheckResult configRedundancyCheckResult
    }
}