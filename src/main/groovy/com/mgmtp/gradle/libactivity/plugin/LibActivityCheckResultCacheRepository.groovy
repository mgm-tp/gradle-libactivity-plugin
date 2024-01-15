package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility
import org.apache.commons.jcs3.access.CacheAccess

/**
 * Provides cache access to read / write lib activity check results.
 */
@PackageScope
@TupleConstructor(post = { NullCheck.ALL_PROPS.call(this) })
@VisibilityOptions(Visibility.PRIVATE)
class LibActivityCheckResultCacheRepository {

    private static final LazyLogger LOGGER = LazyLogger.fromClazz(LibActivityCheckResultCacheRepository.class)

    final CacheAccess<String, LibActivityCheckResultCacheObject> cacheAccess

    @PackageScope
    static LibActivityCheckResultCacheRepository fromCacheAccess(final CacheAccess<String, LibActivityCheckResultCacheObject> cacheAccess) {
        return new LibActivityCheckResultCacheRepository(cacheAccess)
    }

    /**
     * @return Cached check result for lib matching the Maven ID from the provided check result. {@code null} if not
     * cached or if retrieval failed.
     */
    @PackageScope
    LibActivityCheckResultCacheObject getLibActivityCheckResultCacheObject(final MavenId mavenId) {

        try {
            return cacheAccess.get(getCacheKey(mavenId))
        } catch (final Exception e) {
            LOGGER.warn({ "Get cache entry for '${mavenId}' failed." }, e)
            return null
        }
    }

    /**
     * Places the provided check result in cache. It can be found under its Maven ID string representation.
     */
    @PackageScope
    void setLibActivityCheckResultCacheObject(final LibActivityCheckResultCacheObject cacheObject) {

        try {
            cacheAccess.put(getCacheKey(cacheObject), cacheObject)
        } catch (final Exception e) {
            LOGGER.warn({ "Set cache entry for '${cacheObject.checkResult.mavenId}' failed." }, e)
        }
    }

    /**
     * Removes the check result for the provided Maven ID from cache.
     */
    @PackageScope
    void removeLibActivityCheckResultCacheObject(final MavenId mavenId) {

        try {
            cacheAccess.remove(getCacheKey(mavenId))
        } catch (final Exception e) {
            LOGGER.warn({ "Remove cache entry for '${mavenId}' failed." }, e)
        }
    }

    private static String getCacheKey(final LibActivityCheckResultCacheObject cacheObject) {
        return getCacheKey(cacheObject.checkResult.mavenId)
    }

    private static String getCacheKey(final MavenId mavenId) {
        return mavenId as String
    }
}