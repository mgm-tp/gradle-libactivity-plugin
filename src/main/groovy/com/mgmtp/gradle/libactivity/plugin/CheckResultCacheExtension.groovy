package com.mgmtp.gradle.libactivity.plugin

import org.gradle.api.Project

class CheckResultCacheExtension {

    CheckResultCacheExtension(final Project project) {
        dir = project.file(CheckLibActivityTask.DEFAULT_PROJECT_OUTPUT_DIR + '/cache')
    }

    /**
     * The cache directory for check results.
     */
    File dir

    /**
     * Caches check results by the end of the task for the provided maximum number of seconds. This will be measured
     * from the timestamp when the caching procedure starts.
     * <p>
     *     If {@code t1} is the save timestamp of the check result and {@code t2} marks the end of its cache lifespan
     *     then no activity check will take place in the interval {@code [t1, t2]}. Any new release or commit that might
     *     occur in that period will not be queried from an API. The check result for the corresponding lib will be
     *     reported based on the cached data and can be recognized by a "from cache" label. Because API timestamps of the
     *     latest activity of a lib get cached we can judge the activity status without having to query APIs again,
     *     provided the cached result is compatible with the task configuration.
     * </p>
     * <p>
     *     Task configuration settings that influence the compatibility and thus the reusability of a cached check result
     *     are for instance the list of APIs, the activity time tolerance or specific checker configuration, e.g. GitHub
     *     lib mappings. If for example a new API is added and we do not have a cached query result for it then the check
     *     result cannot be built from cache only. Such incompatibilities with the current task configuration will result
     *     in the removal of a cached check result and the lib will take another full activity check with API queries that
     *     produce a new cached result.
     * </p>
     * <p>
     *     Caching aids in saving requests that would most likely return the same answer from a queried API if the task
     *     was executed another time. This is especially useful if only few check results need recheck, e.g. because of a
     *     missing GitHub mapping. We may then execute the task another time with an updated configuration to get an
     *     actual result for the former unavailable that will then be reported together with the cached check results.
     *     Another reason for caching is that it helps save contingent of requests to an API that enforces a rate limit,
     *     e.g. GitHub.
     * </p>
     * <p>
     *     The value in seconds applies globally, which means new results to cache as well as old results that we already
     *     cached will have the specified lifespan.
     * </p>
     * <p>
     *     A value of {@code 0} disables caching. No check result will be retrieved from or stored in cache. A cached
     *     check result for a lib present in the current task run will be removed. As a rule of thumb we do not keep cached
     *     results that we cannot reuse on a task run.
     * </p>
     * <p>
     *     To not just clear the cache for the libs from the current check but to remove th whole cache directory use the
     *     separate {@code deleteCacheDir} task.
     * </p>
     */
    int lifetimeInSeconds = 3600
}