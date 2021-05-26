package com.mgmtp.gradle.libactivity.plugin.result.data.lib

import com.mgmtp.gradle.libactivity.plugin.data.lib.LibDetail
import com.mgmtp.gradle.libactivity.plugin.data.lib.LibTag
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

@TupleConstructor
@VisibilityOptions( Visibility.PRIVATE)
enum LibCheckResultGroupMeta {

    RELEASE_OK(
            Category.ACTIVE,
            SubCategory.RELEASE_OK,
            null,
            [LibTag.ACTIVE, LibTag.RELEASE_OK],
            true),
    AT_LEAST_1_COMMIT(
            Category.ACTIVE,
            SubCategory.AT_LEAST_1_COMMIT,
            LibDetail.NUM_COMMITS,
            [LibTag.ACTIVE, LibTag.OUTDATED_RELEASE, LibTag.AT_LEAST_1_COMMIT],
            true),
    NO_GIT_HUB_COMMIT(
            Category.INACTIVE,
            SubCategory.NO_GIT_HUB_COMMIT,
            LibDetail.LATEST_RELEASE_AGE,
            [LibTag.INACTIVE, LibTag.NO_COMMITS],
            true),
    NO_GIT_HUB_HOSTING(
            Category.INACTIVE,
            SubCategory.NO_GIT_HUB_HOSTING,
            null,
            [LibTag.INACTIVE, LibTag.NO_GITHUB_HOSTING],
            true),
    MOVED(
            Category.INACTIVE,
            SubCategory.MOVED,
            LibDetail.NEW_ADDRESS,
            [LibTag.INACTIVE, LibTag.MOVED],
            false),
    NO_GIT_HUB_MAPPING(
            Category.UNAVAILABLE_RESULT,
            SubCategory.NO_GIT_HUB_MAPPING,
            LibDetail.LATEST_RELEASE_AGE,
            [LibTag.UNAVAILABLE_RESULT, LibTag.NO_GITHUB_MAPPING],
            true),
    GIT_HUB_404(
            Category.UNAVAILABLE_RESULT,
            SubCategory.GIT_HUB_404,
            null,
            [LibTag.UNAVAILABLE_RESULT, LibTag.GITHUB_RESPONSE_404],
            true),
    GIT_HUB_403(
            Category.UNAVAILABLE_RESULT,
            SubCategory.GIT_HUB_403,
            null,
            [LibTag.UNAVAILABLE_RESULT, LibTag.GITHUB_RESPONSE_403],
            true),
    UNKNOWN(
            Category.UNKNOWN,
            null,
            null,
            [LibTag.UNKNOWN],
            true),
    OUTDATED_VERSION(
            Category.OUTDATED_VERSION,
            null,
            LibDetail.VERSION_AGE,
            [LibTag.OUTDATED_VERSION],
            false),
    UNKNOWN_VERSION(
            Category.UNKNOWN_VERSION,
            null,
            null,
            [LibTag.UNKNOWN_VERSION],
            false)

    final Category category

    final SubCategory subCategory

    final LibDetail detail

    final Collection<LibTag> libTags

    final boolean isGroupWithUniqueMembers

    @Override
    String toString( ) {
        return category.name + ( subCategory?.name ? ": ${ subCategory.name}" : '')
    }

    @TupleConstructor
    @VisibilityOptions( Visibility.PRIVATE)
    enum Category {

        ACTIVE( 'ACTIVE'),
        INACTIVE( 'INACTIVE'),
        UNAVAILABLE_RESULT( 'UNAVAILABLE RESULT'),
        UNKNOWN( 'UNKNOWN'),
        OUTDATED_VERSION( 'OUTDATED VERSION'),
        UNKNOWN_VERSION( 'UNKNOWN VERSION')

        final String name
    }

    @TupleConstructor
    @VisibilityOptions( Visibility.PRIVATE)
    enum SubCategory {

        RELEASE_OK( 'latest release within tolerance bounds'),
        AT_LEAST_1_COMMIT( 'latest release outdated but at least 1 commit on GitHub'),
        NO_GIT_HUB_COMMIT( 'no commit on GitHub'),
        NO_GIT_HUB_HOSTING( 'not hosted on GitHub'),
        MOVED( 'library coordinates moved'),
        NO_GIT_HUB_MAPPING( 'no GitHub mapping'),
        GIT_HUB_404( 'GitHub response NOT FOUND'),
        GIT_HUB_403( 'GitHub response FORBIDDEN')

        final String name
    }
}