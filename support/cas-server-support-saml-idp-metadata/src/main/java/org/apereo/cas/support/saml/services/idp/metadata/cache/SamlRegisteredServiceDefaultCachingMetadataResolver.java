package org.apereo.cas.support.saml.services.idp.metadata.cache;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.monitor.Monitorable;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jooq.lambda.Unchecked;
import org.jspecify.annotations.Nullable;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.criterion.SatisfyAnyCriterion;
import org.opensaml.saml.metadata.resolver.ChainingMetadataResolver;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.AbstractDynamicMetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.AbstractMetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.AbstractReloadingMetadataResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.springframework.util.Assert;

/**
 * An adaptation of metadata resolver which handles the resolution of metadata resources
 * inside a cache. It basically is a fancy wrapper around a cache, and constructs the cache
 * semantics before processing the resolution of metadata for a SAML service.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@Monitorable
public class SamlRegisteredServiceDefaultCachingMetadataResolver implements SamlRegisteredServiceCachingMetadataResolver {
    /**
     * The metadata cache, exposed for metrics.
     */
    @Getter
    private final LoadingCache<SamlRegisteredServiceCacheKey, CachedMetadataResolverResult> cache;

    private final CacheLoader<SamlRegisteredServiceCacheKey, CachedMetadataResolverResult> loader;

    private final Duration changeDetectionInterval;

    private final Cache<SamlRegisteredServiceCacheKey, String> failures;

    private final Duration failureCacheExpiration;

    @Getter
    private final OpenSamlConfigBean openSamlConfigBean;

    /**
     * How long a metadata resolver that was removed from the cache stays usable before it is destroyed.
     */
    @Setter(AccessLevel.PACKAGE)
    private Duration metadataResolverRetirementDelay = Duration.ofMinutes(1);

    public SamlRegisteredServiceDefaultCachingMetadataResolver(
        final CasConfigurationProperties casProperties,
        final CacheLoader<SamlRegisteredServiceCacheKey, CachedMetadataResolverResult> loader,
        final OpenSamlConfigBean openSamlConfigBean) {

        this.openSamlConfigBean = openSamlConfigBean;
        this.loader = loader;
        this.changeDetectionInterval = Beans.newDuration(
            casProperties.getAuthn().getSamlIdp().getMetadata().getFileSystem().getChangeDetectionInterval());

        val core = casProperties.getAuthn().getSamlIdp().getMetadata().getCore();
        val metadataCacheExpiration = Beans.newDuration(core.getCacheExpiration());
        this.cache = Caffeine.newBuilder()
            .maximumSize(core.getCacheMaximumSize())
            .recordStats()
            .executor(task -> Thread.ofVirtual().name("saml-metadata-cache").start(task))
            .expireAfter(new SamlRegisteredServiceMetadataExpirationPolicy(metadataCacheExpiration,
                Beans.newDuration(core.getMinimumCacheDuration()), Beans.newDuration(core.getMaximumCacheDuration())))
            .removalListener((_, result, _) -> retireMetadataResolvers(result))
            .build(loader);

        this.failureCacheExpiration = Beans.newDuration(core.getFailureCacheExpiration());
        this.failures = Caffeine.newBuilder()
            .maximumSize(core.getCacheMaximumSize())
            .expireAfterWrite(failureCacheExpiration.isNegative() ? Duration.ZERO : failureCacheExpiration)
            .build();
    }

    /**
     * Resolves the metadata resolver for the service and the requested entity, once.
     * <p>
     * Nothing here is retried. A cached resolver that cannot produce the requested entity will not produce it a
     * moment later, and a source that failed to load is remembered for {@code failure-cache-expiration} rather than
     * consulted again on every request, since the entity id driving the lookup arrives on unauthenticated requests.
     * Transient network failures are retried where they happen, by the HTTP client fetching the document.
     * The one exception is metadata that holds a single service provider and does not hold the requested entity:
     * the document may have changed at its source, so the entry is reloaded once, but not before it has been cached
     * for at least {@code failure-cache-expiration}.
     *
     * @param service     the service
     * @param criteriaSet the criteria set
     * @return the cached metadata resolver result
     * @throws Exception the exception
     */
    @Override
    public CachedMetadataResolverResult resolve(final SamlRegisteredService service, final CriteriaSet criteriaSet) throws Exception {
        val metadataLocation = SpringExpressionLanguageValueResolver.getInstance().resolve(service.getMetadataLocation());
        LOGGER.debug("Resolving metadata for [{}] at [{}]", service.getName(), metadataLocation);
        val cacheKey = new SamlRegisteredServiceCacheKey(service, criteriaSet);
        val failure = failures.getIfPresent(cacheKey);
        if (failure != null) {
            throw new SamlException("%s (failure remembered for %s)".formatted(failure, failureCacheExpiration));
        }
        var queryResult = locateAndCacheMetadataResolver(service, criteriaSet, cacheKey);
        var result = isMetadataResolverAcceptable(queryResult, criteriaSet);
        if (!result.isValid() && shouldReloadMetadata(result)) {
            LOGGER.info("Metadata for [{}] from [{}] holds a single service provider, which is not [{}]; reloading it",
                service.getName(), metadataLocation, criteriaSet);
            cache.invalidate(cacheKey);
            queryResult = locateAndCacheMetadataResolver(service, criteriaSet, cacheKey);
            result = isMetadataResolverAcceptable(queryResult, criteriaSet);
        }
        if (!result.isValid()) {
            LOGGER.warn("SAML metadata resolver [{}] for service [{}] is unable to produce valid metadata from [{}] for [{}]",
                queryResult.getResult().getMetadataResolver().getId(), service.getName(), metadataLocation, criteriaSet);
            throw new SamlException("Unable to locate a valid SAML metadata resolver for "
                + metadataLocation + " to locate " + criteriaSet);
        }
        val cachedResult = queryResult.getResult();
        return result.getEntityDescriptor()
            .<CachedMetadataResolverResult>map(entityDescriptor -> CachedMetadataResolverResult.builder()
                .cachedInstant(cachedResult.getCachedInstant())
                .metadataResolver(cachedResult.getMetadataResolver())
                .entityDescriptor(entityDescriptor)
                .build())
            .orElse(cachedResult);
    }

    @Override
    public void invalidate() {
        LOGGER.trace("Invalidating cache, removing all metadata resolvers");
        cache.invalidateAll();
        failures.invalidateAll();
    }

    @Override
    public void invalidate(final SamlRegisteredService service, final CriteriaSet criteriaSet) {
        LOGGER.trace("Invalidating cache for [{}].", service.getName());
        val cacheKey = new SamlRegisteredServiceCacheKey(service, criteriaSet);
        cache.invalidate(cacheKey);
        failures.invalidate(cacheKey);
    }

    @Override
    public Optional<CachedMetadataResolverResult> getIfPresent(final SamlRegisteredService service, final CriteriaSet criteriaSet) {
        LOGGER.trace("Invalidating cache for [{}] if entry is present.", service.getName());
        val cacheKey = new SamlRegisteredServiceCacheKey(service, criteriaSet);
        return Optional.ofNullable(cache.getIfPresent(cacheKey));
    }

    protected MetadataResolutionResult isMetadataResolverAcceptable(
        final MetadataResolverCacheQueryResult queryResult,
        final CriteriaSet criteriaSet) {
        if (criteriaSet.contains(SatisfyAnyCriterion.class)) {
            return MetadataResolutionResult.builder()
                .entityDescriptor(Optional.empty())
                .valid(true)
                .build();
        }
        val entityDescriptor = queryResult.getEntityDescriptor()
            .orElseGet(Unchecked.supplier(() -> queryResult.getResult().getMetadataResolver().resolveSingle(criteriaSet)));

        return MetadataResolutionResult
            .builder()
            .valid(entityDescriptor != null && entityDescriptor.isValid())
            .entityDescriptor(Optional.ofNullable(entityDescriptor))
            .result(queryResult.getResult())
            .build();
    }

    protected MetadataResolverCacheQueryResult locateAndCacheMetadataResolver(
        final SamlRegisteredService service,
        final CriteriaSet criteriaSet,
        final SamlRegisteredServiceCacheKey cacheKey) {
        LOGGER.debug("Loading metadata resolver from the cache using [{}] for criteria [{}]",
            cacheKey.getCacheKey(), criteriaSet);
        var cacheResult = loadMetadataResolver(service, cacheKey);
        if (cacheResult.haveMetadataSourceFilesChanged(changeDetectionInterval)) {
            LOGGER.info("Metadata files of service [{}] at [{}] have changed and will be reloaded",
                service.getName(), cacheResult.getMetadataSourceFiles());
            cache.invalidate(cacheKey);
            cacheResult = loadMetadataResolver(service, cacheKey);
        }
        LOGGER.debug("Loaded and cached SAML metadata [{}] from [{}]",
            cacheResult.getMetadataResolver().getId(), service.getMetadataLocation());
        refreshIfNearingExpiration(cacheKey, cacheResult);
        Assert.isTrue(cacheResult.isResolved(), "Metadata resolver cannot be found from the cache for " + service.getName());

        return MetadataResolverCacheQueryResult
            .builder()
            .entityDescriptor(Optional.empty())
            .result(cacheResult)
            .build();
    }

    /**
     * Loads the metadata resolver through the cache, remembering a failed load so the source is not consulted again
     * for {@code failure-cache-expiration}.
     *
     * @param service  the service
     * @param cacheKey the cache key
     * @return the cached result
     */
    protected CachedMetadataResolverResult loadMetadataResolver(final SamlRegisteredService service,
                                                                final SamlRegisteredServiceCacheKey cacheKey) {
        try {
            return Objects.requireNonNull(cache.get(cacheKey));
        } catch (final Exception e) {
            val cause = ExceptionUtils.getRootCause(e);
            val metadataLocation = SpringExpressionLanguageValueResolver.getInstance().resolve(service.getMetadataLocation());
            val reason = StringUtils.defaultIfBlank(cause.getMessage(), cause.getClass().getSimpleName());
            LOGGER.warn("Unable to load SAML metadata for service [{}] from [{}]: [{}]. The failure is remembered for [{}]",
                service.getName(), metadataLocation, reason, failureCacheExpiration);
            failures.put(cacheKey, "Unable to load SAML metadata for service [%s] from [%s]: %s"
                .formatted(service.getName(), metadataLocation, reason));
            throw e;
        }
    }

    /**
     * Whether a cached entry that cannot produce the requested entity should be reloaded: only metadata that holds a
     * single service provider, which may have been replaced at its source, and only once the entry has been cached
     * for at least {@code failure-cache-expiration}, so that a caller asking for unknown entities cannot force a
     * reload per request. Whether the metadata holds a single service provider is decided when it is loaded, so a
     * request for an unknown entity does not walk every entity of an aggregate.
     *
     * @param result the resolution result
     * @return true if the entry should be reloaded
     */
    private boolean shouldReloadMetadata(final MetadataResolutionResult result) {
        val cachedResult = result.getResult();
        val age = Duration.between(cachedResult.getCachedInstant(), Instant.now(Clock.systemUTC()));
        if (age.compareTo(failureCacheExpiration) < 0) {
            return false;
        }
        return cachedResult.isSingleServiceProvider();
    }

    /**
     * Reload an entry in the background once three quarters of its lifetime has passed, so the request that would
     * otherwise find it expired is not the one that waits for metadata to be fetched and parsed. The current resolver
     * keeps being served until the reload completes, and a failed reload leaves it in place until it expires.
     *
     * @param cacheKey    the cache key
     * @param cacheResult the cached result
     */
    protected void refreshIfNearingExpiration(final SamlRegisteredServiceCacheKey cacheKey,
                                              final CachedMetadataResolverResult cacheResult) {
        val age = Duration.between(cacheResult.getCachedInstant(), Instant.now(Clock.systemUTC()));
        cache.policy().expireVariably()
            .flatMap(expiration -> expiration.getExpiresAfter(cacheKey))
            .filter(remaining -> remaining.compareTo(age.dividedBy(3)) <= 0)
            .ifPresent(remaining -> {
                LOGGER.debug("Refreshing SAML metadata for [{}] in the background; it expires in [{}]",
                    cacheKey.getRegisteredService().getName(), remaining);
                cache.refresh(cacheKey);
            });
    }

    @SuperBuilder
    @Getter
    @SuppressWarnings("UnusedMethod")
    static final class MetadataResolutionResult {
        private final boolean valid;

        private final Optional<EntityDescriptor> entityDescriptor;

        private final CachedMetadataResolverResult result;
    }

    /**
     * Resolve if present.
     *
     * @param service     the service
     * @param criteriaSet the criteria set
     * @return the resolver.
     */
    Optional<MetadataResolver> resolveIfPresent(final SamlRegisteredService service,
                                                final CriteriaSet criteriaSet) {
        val cacheKey = new SamlRegisteredServiceCacheKey(service, criteriaSet);
        return Optional.ofNullable(cache.getIfPresent(cacheKey)).map(CachedMetadataResolverResult::getMetadataResolver);
    }

    /**
     * Gets statistics.
     *
     * @return the statistics
     */
    CacheStats getCacheStatistics() {
        return cache.stats();
    }

    @Override
    public Map<String, Object> getStatistics() {
        val stats = cache.stats();
        val statistics = new LinkedHashMap<String, Object>();
        statistics.put("size", cache.estimatedSize());
        statistics.put("hitCount", stats.hitCount());
        statistics.put("missCount", stats.missCount());
        statistics.put("hitRate", stats.hitRate());
        statistics.put("loadSuccessCount", stats.loadSuccessCount());
        statistics.put("loadFailureCount", stats.loadFailureCount());
        statistics.put("averageLoadTime", Duration.ofNanos((long) stats.averageLoadPenalty()).toString());
        statistics.put("evictionCount", stats.evictionCount());
        statistics.put("rememberedFailures", failures.estimatedSize());
        return statistics;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Every cached entry is described by the service and entity it was loaded for, when it was loaded and when it
     * expires, the metadata resolvers and local files it came from, what its resolvers report about their sources
     * (such as a metadata backup file and how long ago its source confirmed it), and the failure remembered for it,
     * if any. Remembered failures without a cached entry are listed as well.
     */
    @Override
    public List<Map<String, Object>> getCachedEntries() {
        val entries = new ArrayList<Map<String, Object>>();
        cache.asMap().forEach((cacheKey, result) -> {
            val entry = describe(cacheKey);
            entry.put("cachedInstant", result.getCachedInstant().toString());
            cache.policy().expireVariably()
                .flatMap(expiration -> expiration.getExpiresAfter(cacheKey))
                .ifPresent(expiresIn -> entry.put("expiresIn", expiresIn.toString()));
            entry.put("metadataResolvers", getMetadataResolvers(result).stream()
                .map(metadataResolver -> StringUtils.defaultString(metadataResolver.getId())).toList());
            entry.put("metadataSourceFiles", result.getMetadataSourceFiles().stream().map(File::getAbsolutePath).toList());
            entry.put("singleServiceProvider", result.isSingleServiceProvider());
            if (loader instanceof final SamlRegisteredServiceMetadataResolverCacheLoader metadataLoader) {
                entry.putAll(metadataLoader.describe(cacheKey));
            }
            entries.add(entry);
        });
        failures.asMap().forEach((cacheKey, failure) -> {
            val entry = entries.stream()
                .filter(candidate -> cacheKey.getId().equals(candidate.get("key")))
                .findFirst()
                .orElseGet(() -> {
                    val failedEntry = describe(cacheKey);
                    entries.add(failedEntry);
                    return failedEntry;
                });
            entry.put("rememberedFailure", failure);
        });
        return entries;
    }

    /**
     * Retires the metadata resolvers of an entry that was refreshed, reloaded, evicted, expired or invalidated.
     * Resolvers that run background tasks, which are the reloading and dynamic OpenSAML resolvers, such as one a Groovy
     * script may return, are destroyed once {@link #metadataResolverRetirementDelay} has passed, because requests that
     * obtained the entry before it was removed may still be using them, and only if no cached entry holds the very same
     * resolver by then. Other resolvers hold nothing but parsed metadata and are left to the garbage collector.
     *
     * @param result the cached result that was removed
     */
    protected void retireMetadataResolvers(final @Nullable CachedMetadataResolverResult result) {
        if (result == null) {
            return;
        }
        val retiredResolvers = getMetadataResolvers(result)
            .stream()
            .filter(resolver -> resolver instanceof AbstractReloadingMetadataResolver || resolver instanceof AbstractDynamicMetadataResolver)
            .map(AbstractMetadataResolver.class::cast)
            .toList();
        if (!retiredResolvers.isEmpty()) {
            val delayedExecutor = CompletableFuture.delayedExecutor(metadataResolverRetirementDelay.toMillis(), TimeUnit.MILLISECONDS);
            delayedExecutor.execute(() -> {
                val cachedResolvers = Collections.<MetadataResolver>newSetFromMap(new IdentityHashMap<>());
                cache.asMap().values().forEach(cached -> cachedResolvers.addAll(getMetadataResolvers(cached)));
                retiredResolvers
                    .stream()
                    .filter(resolver -> !resolver.isDestroyed() && !cachedResolvers.contains(resolver))
                    .forEach(resolver -> {
                        LOGGER.debug("Destroying metadata resolver [{}] that is no longer cached", resolver);
                        resolver.destroy();
                    });
            });
        }
    }

    private static List<MetadataResolver> getMetadataResolvers(final CachedMetadataResolverResult result) {
        val metadataResolver = result.getMetadataResolver();
        return metadataResolver instanceof final ChainingMetadataResolver chain
            ? chain.getResolvers()
            : List.of(metadataResolver);
    }

    private static Map<String, Object> describe(final SamlRegisteredServiceCacheKey cacheKey) {
        val service = cacheKey.getRegisteredService();
        val entry = new LinkedHashMap<String, Object>();
        entry.put("key", cacheKey.getId());
        entry.put("registeredServiceId", service.getId());
        entry.put("registeredServiceName", service.getName());
        entry.put("serviceId", service.getServiceId());
        entry.put("metadataLocation", service.getMetadataLocation());
        Optional.ofNullable(cacheKey.getCriteriaSet())
            .map(criteria -> criteria.get(EntityIdCriterion.class))
            .ifPresent(criterion -> entry.put("entityId", criterion.getEntityId()));
        return entry;
    }

    @SuperBuilder
    @Getter
    static final class MetadataResolverCacheQueryResult {
        private final CachedMetadataResolverResult result;

        @Builder.Default
        private final Optional<EntityDescriptor> entityDescriptor = Optional.empty();

        public boolean isValid() {
            return result != null && result.isResolved();
        }
    }
}
