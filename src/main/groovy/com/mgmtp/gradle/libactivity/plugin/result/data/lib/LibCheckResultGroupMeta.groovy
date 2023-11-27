package com.mgmtp.gradle.libactivity.plugin.result.data.lib

import com.mgmtp.gradle.libactivity.plugin.data.lib.Lib
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

@TupleConstructor
@VisibilityOptions(Visibility.PRIVATE)
enum LibCheckResultGroupMeta {

    RELEASE_OK(
            Category.ACTIVE,
            SubCategory.RELEASE_OK,
            null,
            [Lib.Tag.ACTIVE, Lib.Tag.RELEASE_OK],
            true),
    AT_LEAST_1_COMMIT(
            Category.ACTIVE,
            SubCategory.AT_LEAST_1_COMMIT,
            Lib.Detail.NUM_COMMITS,
            [Lib.Tag.ACTIVE, Lib.Tag.OUTDATED_RELEASE, Lib.Tag.AT_LEAST_1_COMMIT],
            true),
    NO_GIT_HUB_COMMIT(
            Category.INACTIVE,
            SubCategory.NO_GIT_HUB_COMMIT,
            Lib.Detail.LATEST_RELEASE_AGE,
            [Lib.Tag.INACTIVE, Lib.Tag.NO_COMMITS],
            true),
    NO_GIT_HUB_HOSTING(
            Category.INACTIVE,
            SubCategory.NO_GIT_HUB_HOSTING,
            null,
            [Lib.Tag.INACTIVE, Lib.Tag.NO_GITHUB_HOSTING],
            true),
    MOVED(
            Category.INACTIVE,
            SubCategory.MOVED,
            Lib.Detail.NEW_ADDRESS,
            [Lib.Tag.INACTIVE, Lib.Tag.MOVED],
            false),
    NO_GIT_HUB_MAPPING(
            Category.UNAVAILABLE_RESULT,
            SubCategory.NO_GIT_HUB_MAPPING,
            Lib.Detail.LATEST_RELEASE_AGE,
            [Lib.Tag.UNAVAILABLE_RESULT, Lib.Tag.NO_GITHUB_MAPPING],
            true),
    GIT_HUB_404(
            Category.UNAVAILABLE_RESULT,
            SubCategory.GIT_HUB_404,
            null,
            [Lib.Tag.UNAVAILABLE_RESULT, Lib.Tag.GITHUB_RESPONSE_404],
            true),
    GIT_HUB_403(
            Category.UNAVAILABLE_RESULT,
            SubCategory.GIT_HUB_403,
            null,
            [Lib.Tag.UNAVAILABLE_RESULT, Lib.Tag.GITHUB_RESPONSE_403],
            true),
    UNKNOWN(
            Category.UNKNOWN,
            null,
            null,
            [Lib.Tag.UNKNOWN],
            true),
    OUTDATED_VERSION(
            Category.OUTDATED_VERSION,
            null,
            Lib.Detail.VERSION_AGE,
            [Lib.Tag.OUTDATED_VERSION],
            false),
    UNKNOWN_VERSION(
            Category.UNKNOWN_VERSION,
            null,
            null,
            [Lib.Tag.UNKNOWN_VERSION],
            false)

    final Category category

    final SubCategory subCategory

    final Lib.Detail detail

    final Collection<Lib.Tag> tags

    final boolean isGroupWithUniqueMembers

    @Override
    String toString() {
        return category.name + (subCategory?.name ? ": ${subCategory.name}" : '')
    }

    @TupleConstructor
    @VisibilityOptions(Visibility.PRIVATE)
    enum Category {

        ACTIVE('ACTIVE'),
        INACTIVE('INACTIVE'),
        UNAVAILABLE_RESULT('UNAVAILABLE RESULT'),
        UNKNOWN('UNKNOWN'),
        OUTDATED_VERSION('OUTDATED VERSION'),
        UNKNOWN_VERSION('UNKNOWN VERSION')

        final String name
    }

    @TupleConstructor
    @VisibilityOptions(Visibility.PRIVATE)
    enum SubCategory {

        RELEASE_OK('latest release within tolerance bounds'),
        AT_LEAST_1_COMMIT('latest release outdated but at least 1 commit on GitHub'),
        NO_GIT_HUB_COMMIT('no commit on GitHub'),
        NO_GIT_HUB_HOSTING('not hosted on GitHub'),
        MOVED('new Maven identifier for library'),
        NO_GIT_HUB_MAPPING('no GitHub mapping'),
        GIT_HUB_404('GitHub response NOT FOUND'),
        GIT_HUB_403('GitHub response FORBIDDEN')

        final String name
    }
}