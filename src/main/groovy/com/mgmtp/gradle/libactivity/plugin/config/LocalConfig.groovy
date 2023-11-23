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

    final int maxAgeLatestReleaseInMonths

    final int maxAgeCurrentVersionInMonths

    final CheckResultOutputFormat outputFormat

    final CheckResultOutputChannel outputChannel

    final File outputFile

    final Map<String, String> localGitHubMappings

    final String gitHubOauthToken

    final Collection<String> xcludes

    final Collection<String> xcludePatterns

    private static final LazyLogger LOGGER = LazyLogger.fromClazz(LocalConfig.class)

    @Builder
    private LocalConfig(
            int maxAgeLatestReleaseInMonths,
            int maxAgeCurrentVersionInMonths,
            CheckResultOutputFormat outputFormat,
            CheckResultOutputChannel outputChannel,
            File outputDir,
            String outputFileName,
            String gitHubOauthToken,
            Map<String, String> localGitHubMappings,
            Collection<String> xcludes,
            Collection<String> xcludePatterns) {
        Objects.requireNonNull(outputFormat)
        Objects.requireNonNull(outputChannel)
        Objects.requireNonNull(outputDir)
        Objects.requireNonNull(outputFileName)
        Objects.requireNonNull(localGitHubMappings)
        Objects.requireNonNull(xcludes)
        Objects.requireNonNull(xcludePatterns)
        LOGGER.info('Initializing local config.')
        this.maxAgeLatestReleaseInMonths = maxAgeLatestReleaseInMonths
        this.maxAgeCurrentVersionInMonths = maxAgeCurrentVersionInMonths
        this.outputFormat = outputFormat
        this.outputChannel = outputChannel
        this.outputFile = this.outputChannel == CheckResultOutputChannel.CONSOLE ? null : newBlankFile(outputDir, outputFileName, this.outputFormat)
        this.gitHubOauthToken = gitHubOauthToken
        this.localGitHubMappings = new HashMap<>(localGitHubMappings)
        this.xcludes = new HashSet<>(xcludes)
        this.xcludePatterns = new HashSet<>(xcludePatterns)
        LOGGER.info { "Local config complete: ${this}" }
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
maxAgeLatestReleaseInMonths: ${maxAgeLatestReleaseInMonths}
maxAgeCurrentVersionInMonths: ${maxAgeCurrentVersionInMonths}
outputFormat: ${outputFormat}
outputChannel: ${outputChannel}
outputFile: ${outputFile ?: 'NONE'}
gitHubOauthToken: ${gitHubOauthToken ? 'YES' : 'NONE'}
localGitHubMappings: ${localGitHubMappings.size()}x
xcludes: ${xcludes.size()}x
xcludePatterns: ${xcludePatterns.size()}x"""
    }
}