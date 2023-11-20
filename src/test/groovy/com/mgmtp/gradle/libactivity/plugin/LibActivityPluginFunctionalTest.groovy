package com.mgmtp.gradle.libactivity.plugin

import com.mgmtp.gradle.libactivity.plugin.result.data.config.LocalConfigCheckResultGroupMeta
import com.mgmtp.gradle.libactivity.plugin.result.data.lib.LibCheckResultGroupMeta
import groovy.json.JsonBuilder
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Files
import java.nio.file.Path

class LibActivityPluginFunctionalTest extends Specification {

    @TempDir
    Path temporaryFolder

    Path testProjectDir

    static final String LIB_COMMONS_LANG_3 = 'org.apache.commons:commons-lang3:3.11'

    static final String LIB_LOGBACK_CLASSIC = 'ch.qos.logback:logback-classic:1.2.3'

    static final String LIB_LOGBACK_CORE = 'ch.qos.logback:logback-core:1.2.3'

    static String TASK_NAME = ':checkLibActivity'

    static String DEFAULT_RESULT_FILE_NAME = 'libactivityReport'

    static final String NEWLINE = System.lineSeparator()

    void setup() {
        testProjectDir = temporaryFolder.resolve('test')
        // create sub-directory
        Files.createDirectories(testProjectDir)
    }

    /**
     * The activity status of libraries is typically inconsistent. What may be active today can be inactive
     * in just a few months. To ensure long-term reproducible test results no classification of active / inactive
     * will take place. Existing libraries must however be locatable, i.e.:
     * <ul>
     *     <li>be part of the check result</li>
     *     <li>not classified UNKNOWN</li>
     *     <li>not classified UNKNOWN_VERSION</li>
     * </ul>
     */
    def 'test valid lib is not UNKNOWN and not UNKNOWN_VERSION'() {
        given:
        setupTestProject(null, LIB_COMMONS_LANG_3)

        when:
        String consoleOutput = captureStdOutFromPlugin()

        then:
        consoleOutput.contains(LIB_COMMONS_LANG_3)

        String expectedUnknownGroup = getNormalizedResultGroup(PlainTextResultGroup.UNKNOWN.headline, LIB_COMMONS_LANG_3)
        String expectedUnknownVersionGroup = getNormalizedResultGroup(PlainTextResultGroup.UNKNOWN_VERSION.headline, LIB_COMMONS_LANG_3)

        String normalizedConsoleOutput = consoleOutput.normalize()
        !normalizedConsoleOutput.contains(expectedUnknownGroup)
        !normalizedConsoleOutput.contains(expectedUnknownVersionGroup)

        String normalizedFileOutput = getNormalizedPlainTextResultFileOutput()
        !normalizedFileOutput.contains(expectedUnknownGroup)
        !normalizedFileOutput.contains(expectedUnknownVersionGroup)
    }

    /**
     * Unknown libraries have a corresponding check result category. They must not be classified
     * UNKNOWN_VERSION as well since we cannot verify the version without valid group and artifact ID.
     */
    def 'test UNKNOWN category'() {
        given:
        String lib_A_B_C = 'a:b:c'
        String lib_X_Y_Z = 'x:y:z'
        setupTestProject(null, lib_X_Y_Z, lib_A_B_C)

        when:
        String consoleOutput = captureStdOutFromPlugin()

        then:
        String expectedUnknownGroup = getNormalizedResultGroup(PlainTextResultGroup.UNKNOWN.headline, lib_A_B_C, lib_X_Y_Z)
        String expectedUnknownVersionGroup = getNormalizedResultGroup(PlainTextResultGroup.UNKNOWN_VERSION.headline, lib_A_B_C, lib_X_Y_Z)

        String normalizedConsoleOutput = consoleOutput.normalize()
        normalizedConsoleOutput.contains(expectedUnknownGroup)
        !normalizedConsoleOutput.contains(expectedUnknownVersionGroup)

        String normalizedFileOutput = getNormalizedPlainTextResultFileOutput()
        normalizedFileOutput.contains(expectedUnknownGroup)
        !normalizedFileOutput.contains(expectedUnknownVersionGroup)
    }

    /**
     * Libraries with valid group and artifact ID will receive a version age check. In case of an
     * unknown version there is a corresponding result category.
     */
    def 'test UNKNOWN_VERSION category'() {
        given:
        String lib_J_UNIT = 'junit:junit:?'
        String lib_SPRING_BOOT = 'org.springframework.boot:spring-boot:*'
        setupTestProject(null, lib_SPRING_BOOT, lib_J_UNIT)

        when:
        String consoleOutput = captureStdOutFromPlugin()

        then:
        String expectedUnknownVersionGroup = getNormalizedResultGroup(PlainTextResultGroup.UNKNOWN_VERSION.headline, lib_J_UNIT, lib_SPRING_BOOT)
        String expectedUnknownGroup = getNormalizedResultGroup(PlainTextResultGroup.UNKNOWN.headline, lib_J_UNIT, lib_SPRING_BOOT)

        String normalizedConsoleOutput = consoleOutput.normalize()
        normalizedConsoleOutput.contains(expectedUnknownVersionGroup)
        !normalizedConsoleOutput.contains(expectedUnknownGroup)

        String normalizedFileOutput = getNormalizedPlainTextResultFileOutput()
        normalizedFileOutput.contains(expectedUnknownVersionGroup)
        !normalizedFileOutput.contains(expectedUnknownGroup)
    }

    /**
     * To exclude a library explicitly from checking 'xcludes' must be used as a parameter. It takes a
     * coordinate tuple GroupId:ArtifactId. The library must then be not part of the check result.
     */
    def 'test Xclude'() {
        given:
        String taskOptionString = "xcludes = ['ch.qos.logback:logback-core']"
        setupTestProject(taskOptionString, LIB_LOGBACK_CORE, LIB_LOGBACK_CLASSIC)

        when:
        String consoleOutput = captureStdOutFromPlugin()

        then:
        !consoleOutput.contains(LIB_LOGBACK_CORE)
        consoleOutput.contains(LIB_LOGBACK_CLASSIC)

        String normalizedFileOutput = getNormalizedPlainTextResultFileOutput()
        !normalizedFileOutput.contains(LIB_LOGBACK_CORE)
        normalizedFileOutput.contains(LIB_LOGBACK_CLASSIC)
    }

    /**
     * To cover a range of excludes, e.g. to exclude libraries with the same group ID the 'xcludePattern'
     * parameter is the right way to go. Like with 'xcludes' matched libraries (GroupID:ArtifactID) must
     * not be part of the check result.
     */
    def 'test Xclude Pattern'() {
        given:
        String taskOptionString = "xcludePatterns = ['ch\\\\.qos\\\\.logback:.*']"
        setupTestProject(taskOptionString, LIB_COMMONS_LANG_3, LIB_LOGBACK_CORE, LIB_LOGBACK_CLASSIC)

        when:
        String consoleOutput = captureStdOutFromPlugin()

        then:
        consoleOutput.contains(LIB_COMMONS_LANG_3)
        !consoleOutput.contains(LIB_LOGBACK_CORE)
        !consoleOutput.contains(LIB_LOGBACK_CLASSIC)

        String normalizedFileOutput = getNormalizedPlainTextResultFileOutput()
        normalizedFileOutput.contains(LIB_COMMONS_LANG_3)
        !normalizedFileOutput.contains(LIB_LOGBACK_CORE)
        !normalizedFileOutput.contains(LIB_LOGBACK_CLASSIC)

    }

    /**
     * To make local config collection parameters maintainable unused entries should be reported
     * to the user.
     */
    def 'test LOCAL CONFIG REDUNDANCY report'() {
        given:
        String gitKey = 'my.fancy/lib'
        String xclude = 'foo:bar'
        String xcludePattern = 'foo.bar:.*'
        String taskOptionString = """
            localGitHubMappings = [
                "${gitKey}":'super-user/all-inclusive-repo'
            ]
            xcludes = [
                "${xclude}"
            ]
            xcludePatterns = [
                "${xcludePattern}"
            ]
        """
        setupTestProject(taskOptionString, LIB_COMMONS_LANG_3)

        when:
        String consoleOutput = captureStdOutFromPlugin()

        then:
        consoleOutput.contains(LIB_COMMONS_LANG_3)

        String unusedGitHubMappings = getNormalizedResultGroup(LocalConfigCheckResultGroupMeta.UNUSED_LOCAL_GIT_HUB_MAPPING_KEYS.name, gitKey)
        String unusedXcludes = getNormalizedResultGroup(LocalConfigCheckResultGroupMeta.UNUSED_XCLUDES.name, xclude)
        String unusedXcludePatterns = getNormalizedResultGroup(LocalConfigCheckResultGroupMeta.UNUSED_XCLUDE_PATTERNS.name, xcludePattern)

        String normalizedConsoleOutput = consoleOutput.normalize()
        normalizedConsoleOutput.contains(unusedGitHubMappings)
        normalizedConsoleOutput.contains(unusedXcludes)
        normalizedConsoleOutput.contains(unusedXcludePatterns)

        String normalizedFileOutput = getNormalizedPlainTextResultFileOutput()
        normalizedFileOutput.contains(unusedGitHubMappings)
        normalizedFileOutput.contains(unusedXcludes)
        normalizedFileOutput.contains(unusedXcludePatterns)
    }

    def 'test JSON output'() {
        given:
        setupTestProject("outputFormat = 'JSON'", LIB_COMMONS_LANG_3)

        when:
        String consoleOutput = captureStdOutFromPlugin()

        then:
        String expectedCoordinates = gavJsonStringFromGavPlainText(LIB_COMMONS_LANG_3)

        String strippedConsoleOutput = stripWhitespace(consoleOutput)
        strippedConsoleOutput.contains(expectedCoordinates)
        !strippedConsoleOutput.contains(LibCheckResultGroupMeta.UNKNOWN.category.name)
        !strippedConsoleOutput.contains(LibCheckResultGroupMeta.UNKNOWN_VERSION.category.name)

        String strippedFileOutput = getStrippedJsonResultFileContent()
        strippedFileOutput.contains(expectedCoordinates)
        !strippedFileOutput.contains(LibCheckResultGroupMeta.UNKNOWN.category.name)
        !strippedFileOutput.contains(LibCheckResultGroupMeta.UNKNOWN_VERSION.category.name)
    }

    def 'test JSON output for file only'() {
        given:
        String taskOptionString = """
            outputFormat = 'JSON'
            outputChannel = 'FILE'
        """
        setupTestProject(taskOptionString, LIB_LOGBACK_CORE, LIB_LOGBACK_CLASSIC)

        when:
        String consoleOutput = captureStdOutFromPlugin()

        then:
        String expectedLogbackCoreCoordinates = gavJsonStringFromGavPlainText(LIB_LOGBACK_CORE)
        String expectedLogbackClassicCoordinates = gavJsonStringFromGavPlainText(LIB_LOGBACK_CLASSIC)
        !stripWhitespace(consoleOutput).contains(expectedLogbackCoreCoordinates)
        !stripWhitespace(consoleOutput).contains(expectedLogbackClassicCoordinates)

        String strippedFileOutput = getStrippedJsonResultFileContent()
        strippedFileOutput.contains(expectedLogbackCoreCoordinates)
        strippedFileOutput.contains(expectedLogbackClassicCoordinates)
        !strippedFileOutput.contains(LibCheckResultGroupMeta.UNKNOWN.category.name)
        !strippedFileOutput.contains(LibCheckResultGroupMeta.UNKNOWN_VERSION.category.name)
    }

    def 'test Rename output file'() {
        given:
        String newFileName = 'myNewFile'
        String newFileExtension = 'txt'
        setupTestProject("outputFileName = '${newFileName}'")

        when:
        checkLibActivity()

        then:
        getResultFile(newFileName, newFileExtension).exists()
        !getResultFile(DEFAULT_RESULT_FILE_NAME, newFileExtension).exists()
    }

    def 'test UP-TO-DATE and OUT-OF-DATE'() {
        given:
        setupTestProject(null, LIB_COMMONS_LANG_3, LIB_LOGBACK_CORE)

        when:
        BuildResult firstResult = checkLibActivity()
        BuildResult secondResult = checkLibActivity()
        replaceDependencyFromBuildGradle(LIB_LOGBACK_CORE, LIB_LOGBACK_CLASSIC)
        BuildResult thirdResult = checkLibActivity()

        then:
        firstResult.task(TASK_NAME).outcome == TaskOutcome.SUCCESS
        secondResult.task(TASK_NAME).outcome == TaskOutcome.UP_TO_DATE
        thirdResult.task(TASK_NAME).outcome == TaskOutcome.SUCCESS
    }

    /** we place the cache dir inside the temp test project since we do not want to reuse cached results from previous runs */
    def 'test FROM-CACHE'() {
        given:
        setupTestProject(null, LIB_COMMONS_LANG_3)
        testProjectDir.resolve('settings.gradle').toFile() << "buildCache.local.directory = '${testProjectDir.toString().replaceAll('\\\\', '/')}/build-cache'"
        testProjectDir.resolve('gradle.properties').toFile() << 'org.gradle.caching=true'

        when:
        BuildResult firstResult = checkLibActivity()
        getResultFile(DEFAULT_RESULT_FILE_NAME, 'txt').delete()
        BuildResult secondResult = checkLibActivity()
        replaceDependencyFromBuildGradle(LIB_COMMONS_LANG_3, LIB_LOGBACK_CORE)
        BuildResult thirdResult = checkLibActivity()

        then:
        firstResult.task(TASK_NAME).outcome == TaskOutcome.SUCCESS
        secondResult.task(TASK_NAME).outcome == TaskOutcome.FROM_CACHE
        thirdResult.task(TASK_NAME).outcome == TaskOutcome.SUCCESS
    }

    private String captureStdOutFromPlugin() {
        return checkLibActivity().output
    }

    private BuildResult checkLibActivity() {
        GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
                .withArguments(TASK_NAME)
                .withPluginClasspath()
                .build()
    }

    private void setupTestProject(String taskOptionString, String... libs) {
        String implementationDependencies = libs.collect { String dependency -> "implementation '${dependency}'" }.join(NEWLINE)
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

    private String getResultFileContent(String fileExtension) {
        getResultFile(DEFAULT_RESULT_FILE_NAME, fileExtension).text
    }

    private File getResultFile(String fileName, String fileExtension) {
        Path taskOutputDir = testProjectDir.resolve('build/libactivity')
        Files.createDirectories(taskOutputDir)
        return taskOutputDir.resolve("${fileName}.${fileExtension}").toFile()
    }

    private void replaceDependencyFromBuildGradle(String dependencyToReplace, String replacementDependency) {
        File buildGradle = testProjectDir.resolve('build.gradle').toFile()
        buildGradle.text = buildGradle.text.replace(dependencyToReplace, replacementDependency)
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
    private static String getNormalizedResultGroup(String groupTitle, String... groupMembers) {
        String headline = "${groupMembers.length}x ${groupTitle}"
        return String.join(NEWLINE,
                '',
                headline,
                '-' * headline.length(),
                groupMembers.join(NEWLINE)
        ).normalize()
    }

    private static String gavJsonStringFromGavPlainText(String gavPlainText) {
        String[] gavTriple = gavPlainText.split(':')
        return new JsonBuilder(coordinates: new JsonBuilder(groupId: gavTriple[0], artifactId: gavTriple[1], version: gavTriple[2]).content)
                .toString()
                .replaceAll('\\{(.*)}', '$1')
    }

    private static String stripWhitespace(String string) {
        return string.replaceAll('\\s', '')
    }

    @TupleConstructor
    @VisibilityOptions(Visibility.PRIVATE)
    private enum PlainTextResultGroup {
        UNKNOWN(LibCheckResultGroupMeta.UNKNOWN as String),
        UNKNOWN_VERSION("${LibCheckResultGroupMeta.UNKNOWN_VERSION as String} (*)")

        final String headline
    }
}