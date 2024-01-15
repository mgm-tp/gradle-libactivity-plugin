package com.mgmtp.gradle.libactivity.plugin


import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction

import java.time.Instant

/**
 * Task for checking the status of "active development" of a lib.
 */
class CheckLibActivityTask extends DefaultTask {

    /**
     * The default directory for any output from the task.
     */
    // @PackageScope ineffective
    protected static final String DEFAULT_PROJECT_OUTPUT_DIR = 'build/libActivity'

    private static final LazyLogger LOGGER = LazyLogger.fromClazz(CheckLibActivityTask.class)

    /**
     * How many days may we look back from the start of check to find the latest activity of a lib, e.g. a release or
     * commit.
     */
    @Input
    int maxDaysSinceLatestActivity = 365

    /**
     * Should dependencies and dependency constraints from the project's {@code buildscript} be checked as well?
     */
    @Input
    boolean checkBuildscript = false

    /**
     * The list of APIs to check in order.
     * <p>
     *     But why check Sonatype first and GitHub later? There are two reasons for that. First, we have technical limitations.
     *     Since GitHub enforces rate limiting we can send only a specific number of requests per time. Though you can
     *     overcome this limitation by configuring the plugin with a personal GitHub token the default configuration will not
     *     work that way which is why most users need to save resources. Moreover, we still depend on (manual) configuration
     *     properties to map a lib (i.e. its Maven coordinates) to a GitHub repo. So we can query commits only for selected
     *     libs.
     * </p>
     * <p>
     *     The second reason is that we assume Sonatype (which queries Maven Central repository) is able to provide results
     *     for more libs than GitHub. Of course this does not mean it covers all of them. There are also cases in which GitHub
     *     knows a lib and Sonatype does not.
     * </p>
     */
    @Input
    List<Api> apis = [Api.SONATYPE, Api.GITHUB]

    /**
     * Collection of check result categories that will cause the task to fail after the activity check has concluded
     * if any of the specified categories are found in any lib check result.
     */
    @Input
    Collection<LibActivityCheckResult.Category> failAtLibActivityCheckResultCategories = EnumSet.of(LibActivityCheckResult.Category.INACTIVE)

    // for testing purposes
    private HttpClient httpClient

    @TaskAction
    void checkLibActivity() {

        // We take the time when the task starts as a reference, i.e. all libs will be checked in the same timeframe.
        final long startOfTaskTimestampEpochMilli = Instant.now().toEpochMilli()

        LOGGER.info('Start "checkLibActivity" task execution. Timestamp: {}', startOfTaskTimestampEpochMilli)

        final Config config = Config.builder()
                .rootProject(project)
                .referenceTimestampEpochMilli(startOfTaskTimestampEpochMilli)
                .maxDaysSinceLatestActivity(maxDaysSinceLatestActivity)
                .checkBuildscript(checkBuildscript)
                .apis(apis)
                .httpClient(httpClient)
                .failAtLibActivityCheckResultCategories(failAtLibActivityCheckResultCategories)
                .build()

        LibActivityCheckManager.fromConfig(config).checkProject(project)
    }
}