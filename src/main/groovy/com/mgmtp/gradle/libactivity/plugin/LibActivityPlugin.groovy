package com.mgmtp.gradle.libactivity.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Evaluation phase of project must have completed for us to collect all dependencies and not just a fraction. Otherwise we would depend on what has already
 * been configured up to when the plugin task gets configured.
 */
class LibActivityPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.afterEvaluate {
            project.tasks.register('checkLibActivity', CheckLibActivity.class) { CheckLibActivity checkLibActivity ->
                checkLibActivity.group = 'checking'
                checkLibActivity.description = 'Checks age of latest library release as well as age of current version in use'
            }
        }
    }
}