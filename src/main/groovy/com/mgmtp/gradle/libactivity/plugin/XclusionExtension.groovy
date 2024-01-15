package com.mgmtp.gradle.libactivity.plugin

class XclusionExtension {

    /**
     * &lt;GROUP_ID&gt;:&lt;ARTIFACT_ID&gt; tuple strings for libs to exclude from the activity check.
     */
    Collection<String> libXcludes = Set.of()

    /**
     * Pattern strings that are matched against &lt;GROUP_ID&gt;:&lt;ARTIFACT_ID&gt; tuple strings of libs collected
     * from the project. Matching libs will not be checked for activity.
     */
    Collection<String> libXcludePatterns = Set.of()
}