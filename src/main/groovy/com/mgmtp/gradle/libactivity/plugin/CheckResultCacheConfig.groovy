package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility
import org.gradle.api.Project

import java.time.Duration

@PackageScope
@TupleConstructor
@VisibilityOptions(Visibility.PRIVATE)
class CheckResultCacheConfig {

    final File dir

    final long lifetimeInMillis

    @Override
    String toString() {
        return 'dir: ' + dir + '; lifetimeInMillis: ' + lifetimeInMillis
    }

    @groovy.transform.NullCheck
    static CheckResultCacheConfig fromProject(final Project project) {

        final CheckResultCacheExtension extension = project.extensions.getByType(CheckResultCacheExtension.class)

        Objects.requireNonNull(extension.dir)

        if (extension.lifetimeInSeconds < 0) {
            throw new IllegalStateException('Cache duration must not be negative')
        }

        return new CheckResultCacheConfig(extension.dir, Duration.ofSeconds(extension.lifetimeInSeconds).toMillis())
    }
}