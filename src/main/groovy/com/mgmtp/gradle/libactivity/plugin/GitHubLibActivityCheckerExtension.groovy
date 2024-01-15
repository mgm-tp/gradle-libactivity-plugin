package com.mgmtp.gradle.libactivity.plugin

class GitHubLibActivityCheckerExtension {

    /**
     * Mappings that translate a Maven ID into a GitHub path. Mappings are required to check commits for a lib on GitHub.
     * These local mappings will be applied in addition to global mappings from the plugin classpath. In case of a key match
     * a local mapping is preferred over a global mapping.
     * <p>
     *     MAPPING KEY := &lt;GROUP_ID&gt;:&lt;ARTIFACT_ID&gt;
     * </p>
     * <p>
     *     MAPPING VALUE := &lt;OWNER&gt;/&lt;REPO&gt;#&lt;DIRS&gt;
     * </p>
     * <p>
     *     where the part starting at # is optional and specifies one or more directories in a GitHub repo.
     * </p>
     */
    Map<String, String> localGitHubMappings = Map.of()

    /**
     * Personal access token to be granted a higher rate limit on GitHub queries.
     */
    String personalAccessToken
}