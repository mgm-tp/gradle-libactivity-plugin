package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope

import static com.mgmtp.gradle.libactivity.plugin.Api.GITHUB
import static com.mgmtp.gradle.libactivity.plugin.Api.SONATYPE
import static com.mgmtp.gradle.libactivity.plugin.LibActivityCheckResult.Category.*

@PackageScope
enum ApiQueryResult {

    CENTRAL_RELEASE_IN_TIMEFRAME(ACTIVE, SONATYPE),
    NO_CENTRAL_RELEASE_IN_TIMEFRAME(INACTIVE, SONATYPE),
    UNKNOWN_TO_CENTRAL(UNKNOWN, SONATYPE),

    GITHUB_COMMIT_IN_TIMEFRAME(ACTIVE, GITHUB),
    NO_GITHUB_COMMIT_IN_TIMEFRAME(INACTIVE, GITHUB),
    MISSING_GITHUB_MAPPING(RECHECK_NEEDED, GITHUB),
    INVALID_GITHUB_MAPPING(RECHECK_NEEDED, GITHUB),
    UNKNOWN_TO_GITHUB(UNKNOWN, GITHUB),

    INVALID_RESPONSE(RECHECK_NEEDED),
    TIMEOUT(RECHECK_NEEDED)

    final LibActivityCheckResult.Category category

    final Set<Api> apis

    /**
     * Constructs a new query result for the given APIs that belongs to the given category. If the APIs are
     * empty the query result is implicitly applicable to all APIs.
     */
    private ApiQueryResult(final LibActivityCheckResult.Category category, final Api... apis) {

        Objects.requireNonNull(category)
        Objects.requireNonNull(apis)

        this.category = category
        this.apis = apis ? EnumSet.of(apis) : EnumSet.allOf(Api.class).asImmutable()
    }
}