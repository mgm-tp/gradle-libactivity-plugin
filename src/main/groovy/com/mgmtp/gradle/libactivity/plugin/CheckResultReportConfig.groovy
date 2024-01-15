package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility
import org.gradle.api.Project

import javax.annotation.Nullable
import java.nio.file.Files

@PackageScope
@TupleConstructor
@VisibilityOptions(Visibility.PRIVATE)
class CheckResultReportConfig {

    final Map<ILibActivityCheckResultGroupKeysProvider, Boolean> libActivityCheckResultGrouping

    final OutputFormat format

    final Set<OutputChannel> channels

    @Nullable
    final File libActivityCheckResultFile

    @Nullable
    final File configRedundancyCheckResultFile

    @Override
    String toString() {

        return libActivityCheckResultGrouping.collect { final ILibActivityCheckResultGroupKeysProvider provider, final Boolean sortReverse ->
            'group by ' + provider + ' - sort ' + (sortReverse ? 'REVERSE' : 'NATURAL')
        }.toString() +
                '; format: ' + format +
                '; channels: ' + channels +
                '; libActivityCheckResultFile: ' + libActivityCheckResultFile +
                '; configRedundancyCheckResultFile: ' + configRedundancyCheckResultFile
    }

    @PackageScope
    @groovy.transform.NullCheck
    static CheckResultReportConfig fromProject(final Project project) {

        final CheckResultReportExtension extension = project.extensions.getByType(CheckResultReportExtension.class)

        // format is checked in extension
        validateGroupLibActivityCheckResultsBy(extension.groupLibActivityCheckResultsBy)
        NullCheck.collection(extension.sortLibActivityCheckResultGroupsReverse)
        validateChannels(extension.channels)
        Objects.requireNonNull(extension.dir)
        Objects.requireNonNull(extension.libActivityCheckResultFileName)
        Objects.requireNonNull(extension.configRedundancyCheckResultFileName)

        final OutputFormat format = extension.format
        final Map<ILibActivityCheckResultGroupKeysProvider, Boolean> libActivityCheckResultGrouping = initLibActivityCheckResultGrouping(
                extension.groupLibActivityCheckResultsBy,
                extension.sortLibActivityCheckResultGroupsReverse)
        final Set<OutputChannel> channels = extension.channels.asImmutable()
        final File dir = initDir(extension.dir, channels)
        final File libActivityCheckResultReportFile = initReportFile(dir, extension.libActivityCheckResultFileName, format)
        final File configRedundancyCheckResultReportFile = initReportFile(dir, extension.configRedundancyCheckResultFileName, format)

        return new CheckResultReportConfig(
                libActivityCheckResultGrouping,
                format,
                channels,
                libActivityCheckResultReportFile,
                configRedundancyCheckResultReportFile)
    }

    private static void validateGroupLibActivityCheckResultsBy(final List<ILibActivityCheckResultGroupKeysProvider> groupKeysProviders) {

        NullCheck.collection(groupKeysProviders)

        final Set<ILibActivityCheckResultGroupKeysProvider> duplicateProviders = groupKeysProviders
                .countBy { final ILibActivityCheckResultGroupKeysProvider provider -> provider }
                .findAll { final ILibActivityCheckResultGroupKeysProvider provider, final Integer times -> times > 1 }
                .keySet()

        if (duplicateProviders) {
            throw new IllegalStateException("List of group keys providers must contain unique elements. Found duplicates: ${duplicateProviders}")
        }
    }

    private static void validateChannels(final Collection<OutputChannel> channels) {

        NullCheck.collection(channels)

        if (!channels) {
            throw new IllegalStateException('Channels for check result output must not be empty')
        }
    }

    @Nullable
    private static File initDir(final File dir, final Set<OutputChannel> channels) {

        if (OutputChannel.FILE in channels) {
            Files.createDirectories(dir.toPath())
            return dir
        }
        return null
    }

    @Nullable
    private static File initReportFile(@Nullable final File dir, final String fileName, final OutputFormat format) {
        return dir ? new File("${dir.toPath().resolve(fileName)}.${format.name().toLowerCase(Locale.US)}") : null
    }

    private static Map<ILibActivityCheckResultGroupKeysProvider, Boolean> initLibActivityCheckResultGrouping(
            final List<ILibActivityCheckResultGroupKeysProvider> groupKeysProviders,
            final Collection<ILibActivityCheckResultGroupKeysProvider> reverseSortProviders) {

        return groupKeysProviders
                .collectEntries { final ILibActivityCheckResultGroupKeysProvider provider -> [(provider): provider in reverseSortProviders] }
                .asImmutable()
    }
}