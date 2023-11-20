package com.mgmtp.gradle.libactivity.plugin

import com.mgmtp.gradle.libactivity.plugin.checker.lib.LibChecker
import com.mgmtp.gradle.libactivity.plugin.config.GlobalConfig
import com.mgmtp.gradle.libactivity.plugin.config.LocalConfig
import com.mgmtp.gradle.libactivity.plugin.data.lib.LibCoordinates
import com.mgmtp.gradle.libactivity.plugin.logging.LazyLogger
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.DependencyConstraint
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependencyConstraint
import org.gradle.api.tasks.*

import java.time.LocalDate
import java.util.stream.Collectors

@CacheableTask
class CheckLibActivity extends DefaultTask {

    @Input
    final Collection<LibCoordinates> libCoordinates = collectCoordinatesFromTargetModuleAndSubmodules()

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

    static final LazyLogger LOGGER = LazyLogger.fromClazz(CheckLibActivity.class)

    @TaskAction
    void checkLibActivity() {
        LOGGER.info('Started task execution.')
        LOGGER.info { "${libCoordinates.size()} libs collected from '${project.name}' + submodules during configuration phase." }
        GlobalConfig globalConfig = GlobalConfig.builder()
                .startOfCheckDate(startOfCheckDate)
                .gitHubPropertiesPathRelativeToClasspath('/github/gitHubMappings.properties')
                .build()
        LocalConfig localConfig = LocalConfig.builder()
                .maxAgeLatestReleaseInMonths(maxAgeLatestReleaseInMonths)
                .maxAgeCurrentVersionInMonths(maxAgeCurrentVersionInMonths)
                .outputFormat(outputFormat)
                .outputChannel(outputChannel)
                .outputDir(outputDir)
                .outputFileName(outputFileName)
                .gitHubOauthToken(gitHubOauthToken)
                .localGitHubMappings(localGitHubMappings)
                .xcludes(xcludes)
                .xcludePatterns(xcludePatterns)
                .build()
        LibChecker.fromConfigBundle(globalConfig, localConfig).checkLibCoordinates(libCoordinates)
    }

    /**
     * <p>
     * Collects dependencies + constraints from resolvable and non-resolvable configurations and turns them into {@link LibCoordinates}.
     * This is motivated by the fact that we care about dependencies consumed by us and about those we provide as consumables to others as well.
     * </p>
     * Dependencies or constraints belonging to the project hierarchy will not be collected because usually they are not published to Maven Central.
     */
    Collection<LibCoordinates> collectCoordinatesFromTargetModuleAndSubmodules() {
        return project.allprojects.stream()
                .flatMap { Project project -> project.configurations.stream() }
                .flatMap { Configuration configuration ->
                    List<LibCoordinates> coordinates = configuration.dependencies
                            .findAll { Dependency dependency -> !(dependency instanceof ProjectDependency) }
                            .findAll { Dependency dependency -> [dependency.group, dependency.name, dependency.version].every { String coordinate -> coordinate } }
                            .collect { Dependency dependency ->
                                LOGGER.debug { "Collecting dependency '${dependency.group}:${dependency.name}:${dependency.version};${configuration.name}'" }
                                new LibCoordinates(dependency.group, dependency.name, dependency.version)
                            }
                    coordinates.addAll(configuration.dependencyConstraints
                            .findAll { DependencyConstraint constraint -> !(constraint instanceof DefaultProjectDependencyConstraint) }
                            .findAll { DependencyConstraint constraint -> [constraint.group, constraint.name, constraint.version].every { String coordinate -> coordinate } }
                            .collect { DependencyConstraint constraint ->
                                LOGGER.debug { "Collecting dependency constraint '${constraint.module}:${constraint.versionConstraint};${configuration.name}'" }
                                new LibCoordinates(constraint.group, constraint.name, constraint.version)
                            })
                    return coordinates.stream()
                }
                .collect(Collectors.toCollection { new TreeSet<LibCoordinates>() })
    }
}