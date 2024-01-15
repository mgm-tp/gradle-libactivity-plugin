package com.mgmtp.gradle.libactivity.plugin

import groovy.json.JsonBuilder
import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Files
import java.nio.file.Path

@PackageScope
@TupleConstructor
@VisibilityOptions(Visibility.PRIVATE)
class LibActivityPluginFunctionalTest extends Specification {

    @TempDir
    private Path temporaryFolder

    private Path testProjectDir

    private static final String LIB_COMMONS_LANG_3_MAVEN_ID = 'org.apache.commons:commons-lang3'

    private static final String LIB_COMMONS_LANG_3 = LIB_COMMONS_LANG_3_MAVEN_ID + ':3.11'

    private static final String LIB_LOGBACK_CLASSIC_MAVEN_ID = 'ch.qos.logback:logback-classic'

    private static final String LIB_LOGBACK_CLASSIC = LIB_LOGBACK_CLASSIC_MAVEN_ID + ':1.2.3'

    private static final String LIB_LOGBACK_CORE_MAVEN_ID = 'ch.qos.logback:logback-core'

    private static final String LIB_LOGBACK_CORE = LIB_LOGBACK_CORE_MAVEN_ID + ':1.2.3'

    private static String TASK_NAME = ':checkLibActivity'

    private static String DEFAULT_RESULT_FILE_NAME = 'libActivityReport'

    private static final String NEWLINE = System.lineSeparator()

    void setup() {
        testProjectDir = temporaryFolder.resolve('test')
        // create sub-directory
        Files.createDirectories(testProjectDir)
    }

    def 'test valid lib is not UNKNOWN'() {

        given:
        setupTestProject(null, LIB_COMMONS_LANG_3)

        when:
        final String consoleOutput = captureStdOutFromTaskExecution()

        then:
        consoleOutput.contains(LIB_COMMONS_LANG_3_MAVEN_ID)

        final String unknownGroup = getNormalizedResultGroup(PlainTextResultGroup.UNKNOWN.headline, LIB_COMMONS_LANG_3_MAVEN_ID)

        final String normalizedConsoleOutput = consoleOutput.normalize()
        !normalizedConsoleOutput.contains(unknownGroup)

        final String normalizedFileOutput = getNormalizedPlainTextResultFileOutput()
        !normalizedFileOutput.contains(unknownGroup)
    }

    private String captureStdOutFromTaskExecution() {
        return checkLibActivity().output
    }

    private BuildResult checkLibActivity() {
        GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
                .withArguments(TASK_NAME, '--info', '--stacktrace')
                .withDebug(true)
                .forwardStdOutput(new PrintWriter(new OutputStreamWriter(System.out, 'UTF-8')))
                .withPluginClasspath()
                .build()
    }

    private void setupTestProject(final String taskOptionString, final String... libs) {

        final String implementationDependencies = libs.collect { final String dependency -> "implementation '${dependency}'" }.join(NEWLINE)
        testProjectDir.resolve('build.gradle').toFile() << """
			plugins {
			    id 'groovy'
				id 'com.mgmtp.gradle-libactivity-plugin'
			}
			dependencies {
			    ${implementationDependencies}
			}
            ${if (taskOptionString) {
            """
                tasks.withType( com.mgmtp.gradle.libactivity.plugin.CheckLibActivity) {
                    ${taskOptionString}
                }
            """
        }}
		"""
    }

    private String getNormalizedPlainTextResultFileOutput() {
        return getResultFileContent('txt').normalize()
    }

    private String getStrippedJsonResultFileContent() {
        return stripWhitespace(getResultFileContent('json'))
    }

    private String getResultFileContent(final String fileExtension) {
        getResultFile(DEFAULT_RESULT_FILE_NAME, fileExtension).text
    }

    private File getResultFile(final String fileName, final String fileExtension) {
        final Path taskOutputDir = testProjectDir.resolve('build/libActivity/output')
        Files.createDirectories(taskOutputDir)
        return taskOutputDir.resolve("${fileName}.${fileExtension}").toFile()
    }

    /**
     * Builds a result group string with normalized line endings like:
     *
     * headline: (group size)x group title
     * underline
     * member_1
     * .
     * .
     * member_N
     *
     */
    private static String getNormalizedResultGroup(final String groupTitle, final String... groupMembers) {
        final String headline = "${groupMembers.length}x ${groupTitle}"
        return String.join(NEWLINE,
                '',
                headline,
                '-' * headline.length(),
                groupMembers.join(NEWLINE)
        ).normalize()
    }

    private static String gavJsonStringFromGavPlainText(final String gavPlainText) {
        final String[] gavTriple = gavPlainText.split(':')
        return new JsonBuilder(groupId: gavTriple[0], artifactId: gavTriple[1])
                .toString()
                .replaceAll('[{}]', '')
    }

    private static String stripWhitespace(final String string) {
        return string.replaceAll('\\s', '')
    }
}