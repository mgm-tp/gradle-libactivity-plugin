package com.mgmtp.gradle.libactivity.plugin.config

import com.mgmtp.gradle.libactivity.plugin.logging.LazyLogger
import com.mgmtp.gradle.libactivity.plugin.result.format.CheckResultOutputFormat
import com.mgmtp.gradle.libactivity.plugin.result.writer.CheckResultOutputChannel
import groovy.transform.builder.Builder

/**
 * Contains properties that can be set by the user when configuring the {@link com.mgmtp.gradle.libactivity.plugin.CheckLibActivity}
 * task locally in a project.
 */
class LocalConfig {

    final int maxDaysSinceLatestRelease

    final int maxDaysSinceCurrentVersionRelease

    final CheckResultOutputFormat outputFormat

    final Collection<CheckResultOutputChannel> outputChannels

    final File outputFile

    final Map<String, String> localGitHubMappings

    final String gitHubOauthToken

    final Collection<String> xcludes

    final Collection<String> xcludePatterns

    private static final LazyLogger LOGGER = LazyLogger.fromClazz(LocalConfig.class)

    @Builder
    private LocalConfig(
            int maxDaysSinceLatestRelease,
            int maxDaysSinceCurrentVersionRelease,
            CheckResultOutputFormat outputFormat,
            File outputDir,
            String outputFileName,
            boolean withConsoleOutput,
            String gitHubOauthToken,
            Map<String, String> localGitHubMappings,
            Collection<String> xcludes,
            Collection<String> xcludePatterns) {
        validateAmountOfDays('maxDaysSinceLatestRelease',maxDaysSinceLatestRelease)
        validateAmountOfDays('maxDaysSinceCurrentVersionRelease',maxDaysSinceCurrentVersionRelease)
        Objects.requireNonNull(outputFormat)
        Objects.requireNonNull(outputDir)
        Objects.requireNonNull(outputFileName)
        Objects.requireNonNull(localGitHubMappings)
        Objects.requireNonNull(xcludes)
        Objects.requireNonNull(xcludePatterns)
        LOGGER.info('Initializing local config.')
        this.maxDaysSinceLatestRelease = maxDaysSinceLatestRelease
        this.maxDaysSinceCurrentVersionRelease = maxDaysSinceCurrentVersionRelease
        this.outputFormat = outputFormat
        this.outputFile = newBlankFile(outputDir, outputFileName, outputFormat)
        outputChannels = withConsoleOutput ? [CheckResultOutputChannel.FILE, CheckResultOutputChannel.CONSOLE] : [CheckResultOutputChannel.FILE]
        this.gitHubOauthToken = gitHubOauthToken
        this.localGitHubMappings = new HashMap<>(localGitHubMappings)
        this.xcludes = new HashSet<>(xcludes)
        this.xcludePatterns = new HashSet<>(xcludePatterns)
        LOGGER.info { "Local config complete: ${this}" }
    }

    private static void validateAmountOfDays(String parameterName, int days) {
        if(days < 1) {
            throw new IllegalArgumentException("Amount of days must be positive. Received '${parameterName} = ${days} days'")
        }
    }

    /** When file output is wanted we clear the target file for a fresh start */
    private static File newBlankFile(File outputDir, String outputFileName, CheckResultOutputFormat outputFormat) {
        File file = new File("${outputDir}/${outputFileName}.${outputFormat.name().toLowerCase()}")
        file.text = ''
        return file
    }

    @Override
    String toString() {
        """
maxDaysSinceLatestRelease: ${maxDaysSinceLatestRelease}
maxDaysSinceCurrentVersionRelease: ${maxDaysSinceCurrentVersionRelease}
outputFormat: ${outputFormat}
outputChannels: ${outputChannels}
outputFile: ${outputFile ?: 'NONE'}
gitHubOauthToken: ${gitHubOauthToken ? 'YES' : 'NONE'}
localGitHubMappings: ${localGitHubMappings.size()}x
xcludes: ${xcludes.size()}x
xcludePatterns: ${xcludePatterns.size()}x"""
    }
}