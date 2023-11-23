package com.mgmtp.gradle.libactivity.plugin

import com.mgmtp.gradle.libactivity.plugin.checker.lib.LibChecker
import com.mgmtp.gradle.libactivity.plugin.config.GlobalConfig
import com.mgmtp.gradle.libactivity.plugin.config.LocalConfig
import com.mgmtp.gradle.libactivity.plugin.data.lib.MavenIdentifier
import com.mgmtp.gradle.libactivity.plugin.logging.LazyLogger
import com.mgmtp.gradle.libactivity.plugin.result.format.CheckResultOutputFormat
import com.mgmtp.gradle.libactivity.plugin.result.writer.CheckResultOutputChannel
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.*
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependencyConstraint
import org.gradle.api.tasks.*

import java.time.LocalDate

@CacheableTask
class CheckLibActivity extends DefaultTask {

    @Input
    final Collection<MavenIdentifier> libMavenIdentifiers = collectExternalLibMavenIdentifiersFromTargetModuleAndSubmodules()

    @Input
    int maxAgeLatestReleaseInMonths = 12

    @Input
    int maxAgeCurrentVersionInMonths = 60

    @Input
    final LocalDate startOfCheckDate = LocalDate.now()

    @Input
    String outputFormat = 'TXT'

    @Input
    String outputChannel = 'DUAL'

    @Input
    String outputFileName = 'libactivityReport'

    @Input
    @Optional
    String gitHubOauthToken

    @Input
    @Optional
    Map<String, String> localGitHubMappings = [:]

    @Input
    @Optional
    Collection<String> xcludes = []

    @Input
    @Optional
    Collection<String> xcludePatterns = []

    @OutputDirectory
    File outputDir = project.file('build/libactivity')

    private static final LazyLogger LOGGER = LazyLogger.fromClazz(CheckLibActivity.class)

    @TaskAction
    void checkLibActivity() {
        LOGGER.info('Started task execution.')
        LOGGER.info { "${libMavenIdentifiers.size()} libs collected from '${project.name}' + submodules during configuration phase." }
        GlobalConfig globalConfig = GlobalConfig.builder()
                .startOfCheckDate(startOfCheckDate)
                .gitHubPropertiesPathRelativeToClasspath('/github/gitHubMappings.properties')
                .build()
        LocalConfig localConfig = LocalConfig.builder()
                .maxAgeLatestReleaseInMonths(maxAgeLatestReleaseInMonths)
                .maxAgeCurrentVersionInMonths(maxAgeCurrentVersionInMonths)
                .outputFormat(CheckResultOutputFormat.parse(outputFormat))
                .outputChannel(CheckResultOutputChannel.parse(outputChannel))
                .outputDir(outputDir)
                .outputFileName(outputFileName)
                .gitHubOauthToken(gitHubOauthToken)
                .localGitHubMappings(localGitHubMappings)
                .xcludes(xcludes)
                .xcludePatterns(xcludePatterns)
                .build()
        LibChecker.fromConfigBundle(globalConfig, localConfig).checkLibMavenIdentifiers(libMavenIdentifiers)
    }

    /**
     * <p>
     * Collects dependencies + constraints from resolvable and non-resolvable configurations and turns them into {@link MavenIdentifier}.
     * This is motivated by the fact that we care about dependencies consumed by us and about those we provide as consumables to others as well.
     * </p>
     * <p>
     * Only external dependencies and constraints are taken into account. This is why project dependencies will not be collected. It is common that the task
     * executor has influence on such internal dependencies and their active development.
     * </p>
     * <p>
     * Returns a sorted list of library Maven identifiers.
     * </p>
     */
    private List<MavenIdentifier> collectExternalLibMavenIdentifiersFromTargetModuleAndSubmodules() {

        return project.allprojects.collect { Project project -> project.configurations }
                .collect { ConfigurationContainer configurationContainer -> configurationContainer.getAsMap().values() }
                .flatten()
                .collect { Configuration configuration ->
                    collectNonProjectDependencyMavenIdentifiers(configuration) + collectNonProjectDependencyConstraintMavenIdentifiers(configuration)
                }
                .flatten()
                .unique()
                .sort()
    }

    static List<MavenIdentifier> collectNonProjectDependencyMavenIdentifiers(Configuration configuration) {

        return configuration.dependencies
                .findAll { Dependency dependency -> !(dependency instanceof ProjectDependency) }
                .collect { Dependency dependency ->
                    LOGGER.debug { "Collecting dependency '${dependency.group}:${dependency.name}:${dependency.version};${configuration.name}'" }
                    new MavenIdentifier(dependency.group, dependency.name, dependency.version)
                }
    }

    static List<MavenIdentifier> collectNonProjectDependencyConstraintMavenIdentifiers(Configuration configuration) {

        return configuration.dependencyConstraints
                .findAll { DependencyConstraint constraint -> !(constraint instanceof DefaultProjectDependencyConstraint) }
                .collect { DependencyConstraint constraint ->
                    LOGGER.debug { "Collecting dependency constraint '${constraint.module}:${constraint.versionConstraint};${configuration.name}'" }
                    new MavenIdentifier(constraint.group, constraint.name, constraint.version)
                }
    }
}