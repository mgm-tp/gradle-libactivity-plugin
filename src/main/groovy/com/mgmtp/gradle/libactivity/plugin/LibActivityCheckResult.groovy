package com.mgmtp.gradle.libactivity.plugin


import groovy.transform.EqualsAndHashCode
import groovy.transform.PackageScope
import groovy.transform.TupleConstructor
import groovy.transform.VisibilityOptions
import groovy.transform.options.Visibility

@EqualsAndHashCode
@TupleConstructor(post = { NullCheck.ALL_PROPS.call(this) })
@VisibilityOptions(Visibility.PRIVATE)
class LibActivityCheckResult implements Comparable<LibActivityCheckResult>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L

    final MavenId mavenId

    final Map<Api, DetailedApiQueryResult> detailedApiQueryResults

    final CacheData cacheData

    final Category category

    final boolean fromCache

    @PackageScope
    static class Builder {

        private MavenId mavenId

        private final Map<Api, DetailedApiQueryResult> apiQueryResults = new HashMap<>()

        private final CacheData.Builder cacheDataBuilder = CacheData.builder()

        private boolean fromCache

        @PackageScope
        Builder mavenId(final MavenId mavenId) {
            this.mavenId = mavenId
            return this
        }

        @PackageScope
        Builder detailedApiQueryResults(final Map<Api, DetailedApiQueryResult> apiQueryResults) {
            NullCheck.map(apiQueryResults)
            this.apiQueryResults.putAll(apiQueryResults)
            return this
        }

        @PackageScope
        Builder cacheData(final CacheData cacheData) {

            cacheDataBuilder
                    .checkerConfigCacheData(cacheData.checkerConfigCacheData)
                    .queryResultCacheData(cacheData.queryResultCacheData)

            return this
        }

        @PackageScope
        Builder fromCache(final boolean fromCache) {
            this.fromCache = fromCache
            return this
        }

        @PackageScope
        LibActivityCheckResult build() {

            if (!apiQueryResults) {
                throw new IllegalStateException('Cannot build lib activity check result without API query result.')
            }
            // In case of multiple API query results we collect and sort connected categories. Because the category enum
            // applies a priority ordering the first category (with minimal ordinal) wins
            final Category category = apiQueryResults.values()
                    .collect { final DetailedApiQueryResult detailedApiQueryResult -> detailedApiQueryResult.queryResult.category }
                    .min()

            return new LibActivityCheckResult(mavenId, apiQueryResults.asImmutable(), cacheDataBuilder.build(), category, fromCache)
        }
    }

    @PackageScope
    static Builder builder() {
        return new Builder()
    }

    @PackageScope
    Builder toBuilder() {

        return builder()
                .mavenId(mavenId)
                .detailedApiQueryResults(detailedApiQueryResults)
                .cacheData(cacheData)
                .fromCache(fromCache)
    }

    @PackageScope
    static class SingleApiBuilder {

        private final Api api

        private MavenId mavenId

        private final DetailedApiQueryResult.Builder detailedApiQueryResultBuilder = DetailedApiQueryResult.builder()

        private final CacheData.Builder cacheDataBuilder = CacheData.builder()

        private SingleApiBuilder(final Api api) {
            Objects.requireNonNull(api)
            this.api = api
        }

        @PackageScope
        SingleApiBuilder mavenId(final MavenId mavenId) {
            this.mavenId = mavenId
            return this
        }

        @PackageScope
        SingleApiBuilder apiQueryResult(final ApiQueryResult apiQueryResult) {
            detailedApiQueryResultBuilder.apiQueryResult(apiQueryResult)
            return this
        }

        @PackageScope
        SingleApiBuilder detailedApiQueryResult(final ApiQueryResult apiQueryResult, final ApiQueryResultDetail detailKey, final Comparable<?> detailValue) {
            detailedApiQueryResultBuilder.apiQueryResult(apiQueryResult).apiQueryResultDetail(detailKey, detailValue)
            return this
        }

        @PackageScope
        SingleApiBuilder apiQueryResultCacheData(final IApiQueryResultCacheData apiQueryResultCacheData) {
            cacheDataBuilder.queryResultCacheData(api, apiQueryResultCacheData)
            return this
        }

        @PackageScope
        SingleApiBuilder checkerConfigCacheData(final IApiLibActivityCheckerConfigCacheData checkerConfigCacheData) {
            cacheDataBuilder.checkerConfigCacheData(api, checkerConfigCacheData)
            return this
        }

        @PackageScope
        LibActivityCheckResult build() {

            final DetailedApiQueryResult detailedApiQueryResult = detailedApiQueryResultBuilder.build()
            final CacheData cacheData = cacheDataBuilder.build()
            final Category category = detailedApiQueryResult.queryResult.category

            return new LibActivityCheckResult(mavenId, [(api): detailedApiQueryResult].asImmutable(), cacheData, category)
        }
    }

    @PackageScope
    static SingleApiBuilder singleApiBuilder(final Api api) {
        return new SingleApiBuilder(api)
    }

    @Override
    int compareTo(final LibActivityCheckResult anotherCheckResult) {
        return this.mavenId <=> anotherCheckResult.mavenId
    }

    /**
     * This enum applies a priority ordering, i.e. if a lib has API query results that belong to different categories
     * the topmost category is the most relevant and becomes the check result category.
     */
    @PackageScope
    enum Category {

        /**
         * There is active development for a lib. We can find a latest activity timestamp in this category to prove that.
         */
        ACTIVE,
        /**
         * The check procedure was interrupted by a (temporary) error, e.g. API call timeout, request rate limit exceeded.
         * To collect a result we need to rerun the lib check.
         */
        RECHECK_NEEDED,
        /**
         * No sign of active development could be found for a lib. There is either no latest activity timestamp or one
         * outside the activity timeframe.
         */
        INACTIVE,
        /**
         * The lib is unknown to an API. There is the chance that the lib may become known to the API after a future check.
         * Yet, this will most likely take some time.
         */
        UNKNOWN
    }

    /**
     * For validation of this check result when restored from cache.
     */
    @PackageScope
    @EqualsAndHashCode
    @TupleConstructor(post = { NullCheck.ALL_PROPS.call(this) })
    @VisibilityOptions(Visibility.PRIVATE)
    static class CacheData implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L

        final Map<Api, IApiQueryResultCacheData> queryResultCacheData

        final Map<Api, IApiLibActivityCheckerConfigCacheData> checkerConfigCacheData

        @PackageScope
        static Builder builder() {
            return new Builder()
        }

        @PackageScope
        @TupleConstructor
        @VisibilityOptions(Visibility.PRIVATE)
        static class Builder {

            final Map<Api, IApiQueryResultCacheData> queryResultCacheData = new HashMap<>()

            final Map<Api, IApiLibActivityCheckerConfigCacheData> checkerConfigCacheData = new HashMap<>()

            @PackageScope
            CacheData.Builder queryResultCacheData(final Api api, final IApiQueryResultCacheData queryResultCacheData) {

                Objects.requireNonNull(api)
                Objects.requireNonNull(queryResultCacheData)

                this.queryResultCacheData[api] = queryResultCacheData
                return this
            }

            @PackageScope
            CacheData.Builder queryResultCacheData(final Map<Api, IApiQueryResultCacheData> queryResultCacheData) {
                NullCheck.map(queryResultCacheData)
                this.queryResultCacheData.putAll(queryResultCacheData)
                return this
            }

            @PackageScope
            CacheData.Builder checkerConfigCacheData(final Api api, final IApiLibActivityCheckerConfigCacheData checkerConfigCacheData) {

                Objects.requireNonNull(api)
                Objects.requireNonNull(checkerConfigCacheData)

                this.checkerConfigCacheData[api] = checkerConfigCacheData
                return this
            }

            @PackageScope
            CacheData.Builder checkerConfigCacheData(final Map<Api, IApiLibActivityCheckerConfigCacheData> checkerConfigCacheData) {
                NullCheck.map(checkerConfigCacheData)
                this.checkerConfigCacheData.putAll(checkerConfigCacheData)
                return this
            }

            @PackageScope
            CacheData build() {
                return new CacheData(queryResultCacheData.asImmutable(), checkerConfigCacheData.asImmutable())
            }
        }
    }
}