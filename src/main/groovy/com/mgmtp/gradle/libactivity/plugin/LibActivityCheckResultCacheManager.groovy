package com.mgmtp.gradle.libactivity.plugin

import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility
import org.apache.commons.jcs3.JCS
import org.apache.commons.jcs3.access.CacheAccess

import javax.annotation.Nullable
import java.time.Instant

import static com.mgmtp.gradle.libactivity.plugin.LibActivityCheckResult.Category.RECHECK_NEEDED

/**
 * Initializes and tears down JCS cache. Caches and retrieves a collection of lib activity check results. Updates outdated
 * information in check results after retrieval.
 */
@PackageScope
@TupleConstructor(post = { NullCheck.ALL_PROPS.call(this) })
@VisibilityOptions(Visibility.PRIVATE)
class LibActivityCheckResultCacheManager {

    private static final String LIB_ACTIVITY_CACHE_REGION = 'LIBACTIVITY'

    private static final LazyLogger LOGGER = LazyLogger.fromClazz(LibActivityCheckResultCacheManager.class)

    final Config config

    final CheckResultCacheConfig cacheConfig

    final LibActivityCheckResultCacheRepository cacheRepository

    @PackageScope
    static LibActivityCheckResultCacheManager fromConfig(final Config config) {

        final CheckResultCacheConfig cacheConfig = config.checkResultCacheConfig
        final CacheAccess<String, LibActivityCheckResultCacheObject> cacheAccess = getConfiguredCacheAccess(cacheConfig)
        final LibActivityCheckResultCacheRepository cacheRepository = LibActivityCheckResultCacheRepository.fromCacheAccess(cacheAccess)

        return new LibActivityCheckResultCacheManager(config, cacheConfig, cacheRepository)
    }

    /**
     * Gets all check results from cache that match the provided. If there are cached results their details will be updated
     * as well as the unused GitHub mapping keys from the current redundant local cacheConfig check result.
     */
    @PackageScope
    Set<CheckResultBundle> getUpdatedCachedCheckResults(final Collection<MavenId> mavenIds) {

        if (cacheConfig.lifetimeInMillis == 0) {
            LOGGER.info { "Caching is disabled. Remove check results for ${mavenIds.size()} libs from cache if present." }
            // if caching is not wanted we do not keep a cached check result anymore because this task run will produce a newer result => applies only
            // to libs that should be checked
            mavenIds.each { final MavenId mavenId -> cacheRepository.removeLibActivityCheckResultCacheObject(mavenId) }
            return Set.of()
        }

        LOGGER.info { "Check cache for ${mavenIds.size()} check results." }

        final Set<CheckResultBundle> checkResultsFromCache = mavenIds.collect { final MavenId mavenId -> getCachedCheckResult(mavenId) }.findAll().toSet()

        LOGGER.info { "Retrieved ${checkResultsFromCache.size()} check results from cache." }
        return checkResultsFromCache
    }

    /**
     * We first load the global caching properties from the classpath and then add other properties that can be set by the
     * user.
     *
     * @return Cache access with user-defined runtime properties.
     */
    private static CacheAccess<String, LibActivityCheckResultCacheObject> getConfiguredCacheAccess(final CheckResultCacheConfig cacheConfig) {

        final Properties jcsProps = new Properties()
        loadJcsConfig(jcsProps)
        // cache disk directory
        jcsProps["jcs.auxiliary.${LIB_ACTIVITY_CACHE_REGION}.attributes.DiskPath"] = cacheConfig.dir as String
        JCS.setConfigProperties(jcsProps)

        return JCS.getInstance(LIB_ACTIVITY_CACHE_REGION)
    }

    private static void loadJcsConfig(final Properties jcsProps) {

        try {
            LOGGER.info('Load JCS cacheConfig properties from classpath.')
            jcsProps.load(LibActivityCheckResultCacheManager.class.getResourceAsStream('/cache/libActivityCheckResultCache.properties'))
        } catch (final Exception e) {
            throw new IllegalStateException('Read JCS cacheConfig properties caused error.', e)
        }
    }

    @Nullable
    private CheckResultBundle getCachedCheckResult(final MavenId mavenId) {

        final LibActivityCheckResult checkResultFromCache = getCheckResultFromCache(mavenId)

        if (checkResultFromCache) {

            final CheckResultBundle updatedCheckResult = LibActivityCheckResultFromCacheUpdateManager.fromConfig(config)
                    .updateCheckResultFromCache(checkResultFromCache)

            if (updatedCheckResult) {
                LOGGER.debug { "Found reusable cache entry for '${mavenId}'." }
                return updatedCheckResult
            }
        }

        LOGGER.debug { "Found stale cache entry for '${mavenId}' which will be removed." }
        // we remove stale results from cache because even though most new check results would update existing ones from cache there might also be new
        // results that need recheck / that will not be cached
        cacheRepository.removeLibActivityCheckResultCacheObject(mavenId)

        return null
    }

    @Nullable
    private LibActivityCheckResult getCheckResultFromCache(final MavenId mavenId) {

        final LibActivityCheckResultCacheObject cacheObject = cacheRepository.getLibActivityCheckResultCacheObject(mavenId)

        if (cacheObject && isCacheObjectAlive(cacheObject)) {
            return cacheObject.checkResult
        }

        return null
    }

    private boolean isCacheObjectAlive(final LibActivityCheckResultCacheObject cacheObject) {
        final long maxTimestampEpochMilli = cacheObject.saveTimestampEpochMilli + cacheConfig.lifetimeInMillis
        return config.activityTimeframeConfig.timeframe.maximum <= maxTimestampEpochMilli
    }

    /**
     * Caches those of the provided check results that are eligible for caching. Currently all check results that do not
     * belong to the RECHECK_NEEDED category can be cached. This is because unavailable results come from short-term errors
     * that can either be fixed by the user or should not take long to disappear.
     */
    @PackageScope
    void cacheCheckResults(final Collection<LibActivityCheckResult> checkResults) {

        if (cacheConfig.lifetimeInMillis == 0) {
            LOGGER.info("Caching is disabled. None of provided ${checkResults.size()} check results will be cached.")
            return
        }
        if (checkResults) {

            LOGGER.info { "Check if ${checkResults.size()} check results are cacheable." }

            // a check result can be cached if it is a final result
            final Collection<LibActivityCheckResult> cacheableCheckResults = checkResults.findAll { final LibActivityCheckResult checkResult ->
                RECHECK_NEEDED != checkResult.category
            }

            if (cacheableCheckResults) {
                final long saveTimestamp = Instant.now().toEpochMilli()
                LOGGER.info { "Discovered ${cacheableCheckResults.size()} cacheable check results. Cache all with save timestamp ${saveTimestamp}." }
                cacheableCheckResults.each { final LibActivityCheckResult cacheableCheckResult -> cacheCheckResult(cacheableCheckResult, saveTimestamp) }
            } else {
                LOGGER.info('No cacheable check results found. Nothing to cache.')
            }
        } else {
            LOGGER.info('Provided check results are empty. Nothing to cache.')
        }
    }

    private void cacheCheckResult(final LibActivityCheckResult checkResult, final long saveTimestampEpochMilli) {
        LOGGER.debug { "Set cache entry for ${checkResult.mavenId}." }
        final LibActivityCheckResultCacheObject cacheObject = new LibActivityCheckResultCacheObject(saveTimestampEpochMilli, checkResult)
        cacheRepository.setLibActivityCheckResultCacheObject(cacheObject)
    }

    @PackageScope
    void shutdown() {

        try {
            LOGGER.info('Shutdown lib activity check result cache.')
            JCS.shutdown()
        } catch (final Exception e) {
            // Despite #shutdown() signature gives no hint on possible exceptions we should inform the user that a
            // manual cleanup makes sense if daemons are used. Because this is a default since Gradle 3.0 most users
            // would be affected. The problem here is that we don't know the state of the CompositeCacheManager singleton
            // inside JCS. This instance remains part of the JVM as long as the classloader does not unload the JCS class
            // (see similar issue https://issues.apache.org/jira/browse/JCS-199).
            LOGGER.warn('Cache shutdown failed. If Gradle daemons were active you may want to kill them to prevent reuse of a possibly corrupted cache manager.', e)
        }
    }
}