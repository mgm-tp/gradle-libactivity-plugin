package com.mgmtp.gradle.libactivity.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.TaskProvider

class LibActivityPlugin implements Plugin<Project> {

    @Override
    void apply(final Project project) {

        project.extensions.create('xclusions', XclusionExtension.class)
        project.extensions.create('gitHubChecker', GitHubLibActivityCheckerExtension.class)
        project.extensions.create('report', CheckResultReportExtension.class, project)
        final CheckResultCacheExtension checkResultCacheExtension = project.extensions.create('cache', CheckResultCacheExtension.class, project)

        final String checkLibActivityTaskName = 'checkLibActivity'
        final String libActivityTaskGroup = 'lib activity'

        final TaskProvider<CheckLibActivityTask> checkLibActivityTaskProvider = registerCheckLibActivityTask(checkLibActivityTaskName, libActivityTaskGroup, project)

        registerDeleteCacheDir(checkLibActivityTaskProvider, libActivityTaskGroup, checkResultCacheExtension, project)
    }

    private static TaskProvider<CheckLibActivityTask> registerCheckLibActivityTask(
            final String checkLibActivityTaskName, final String libActivityTaskGroup, final Project project) {

        return project.tasks
                .register(checkLibActivityTaskName, CheckLibActivityTask.class) { final CheckLibActivityTask checkLibActivityTask ->

                    checkLibActivityTask.group = libActivityTaskGroup
                    checkLibActivityTask.description = 'Checks active development of a library, i.e. its latest release should not be older than a specified number of days or there should be at least one commit on GitHub within that timeframe'
                    // we use JCS caching in the task because not each check result from the produced collection of results is cacheable and
                    // because caching should work on a "limited time" basis
                    checkLibActivityTask.outputs.upToDateWhen { false }
                }
    }

    private static void registerDeleteCacheDir(
            final TaskProvider<CheckLibActivityTask> checkLibActivityTaskProvider,
            final String libActivityTaskGroup,
            final CheckResultCacheExtension checkResultCacheExtension,
            final Project project) {

        final String deleteCacheDirTaskName = 'deleteCacheDir'

        project.tasks.register(deleteCacheDirTaskName, Delete) { final Delete delete ->

            delete.group = libActivityTaskGroup
            delete.description = 'Removes the cache directory for lib activity check results'

            // create + configure check task to evaluate cache extension
            checkLibActivityTaskProvider.get()
            // check task has been configured => extension property can give us the user-specific cache dir
            delete.delete checkResultCacheExtension.dir
        }
    }
}