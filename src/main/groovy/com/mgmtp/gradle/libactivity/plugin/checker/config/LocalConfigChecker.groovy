package com.mgmtp.gradle.libactivity.plugin.checker.config

import com.mgmtp.gradle.libactivity.plugin.config.LocalConfig
import com.mgmtp.gradle.libactivity.plugin.logging.LazyLogger
import com.mgmtp.gradle.libactivity.plugin.result.data.config.LocalConfigCheckResult
import com.mgmtp.gradle.libactivity.plugin.result.data.config.LocalConfigCheckResultGroup
import com.mgmtp.gradle.libactivity.plugin.result.data.config.LocalConfigCheckResultGroupMeta

/** For maintenance of local config. Collects redundant entries. */
class LocalConfigChecker {

    final Collection<String> unusedLocalGitHubMappingKeys

    final Collection<String> unusedXcludes

    final Collection<String> unusedXcludePatterns

    private static final LazyLogger LOGGER = LazyLogger.fromClazz(LocalConfigChecker.class)

    private static final Closure<String> MARKED_AS_USED_LOG = { String item, Object value ->
        "Local config item '${item}' with value '${value}' is marked USED"
    }

    private LocalConfigChecker(LocalConfig localConfig) {
        Objects.requireNonNull(localConfig)
        this.unusedLocalGitHubMappingKeys = new HashSet<>(localConfig.localGitHubMappings.keySet())
        this.unusedXcludes = new HashSet<>(localConfig.xcludes)
        this.unusedXcludePatterns = new HashSet<>(localConfig.xcludePatterns)
    }

    static LocalConfigChecker fromLocalConfig(LocalConfig localConfig) {
        return new LocalConfigChecker(localConfig)
    }

    void markLocalGitHubMappingKeyAsUsed(String gitHubMappingKey) {
        LOGGER.debug { MARKED_AS_USED_LOG.call("gitHubMappingKey", gitHubMappingKey) }
        unusedLocalGitHubMappingKeys.remove(gitHubMappingKey)
    }

    void markXcludeAsUsed(String xclude) {
        LOGGER.debug { MARKED_AS_USED_LOG.call("xclude", xclude) }
        unusedXcludes.remove(xclude)
    }

    void markXcludePatternAsUsed(String xcludePattern) {
        LOGGER.debug { MARKED_AS_USED_LOG.call("xcludePattern", xcludePattern) }
        unusedXcludePatterns.remove(xcludePattern)
    }

    LocalConfigCheckResult getResult() {
        return LocalConfigCheckResult.fromGroups([
                LocalConfigCheckResultGroup.fromGroupMetaAndFindings(LocalConfigCheckResultGroupMeta.UNUSED_LOCAL_GIT_HUB_MAPPING_KEYS, unusedLocalGitHubMappingKeys),
                LocalConfigCheckResultGroup.fromGroupMetaAndFindings(LocalConfigCheckResultGroupMeta.UNUSED_XCLUDE_PATTERNS, unusedXcludePatterns),
                LocalConfigCheckResultGroup.fromGroupMetaAndFindings(LocalConfigCheckResultGroupMeta.UNUSED_XCLUDES, unusedXcludes)
        ].findAll { LocalConfigCheckResultGroup group -> group.members })
    }
}