package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope

@PackageScope
interface ILibActivityCheckResultGroupKeysProvider extends IGroupKeysProvider<Comparable<?>, LibActivityCheckResult> {
    // marker interface to pin generics related to grouping lib activity check results
}