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
import com.google.common.collect.Iterables;
import lombok.Builder;
import lombok.Getter;
import lombok.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jooq.lambda.Unchecked;
import org.opensaml.core.criterion.SatisfyAnyCriterion;
import org.opensaml.saml.metadata.criteria.entity.impl.EvaluableEntityRoleEntityDescriptorCriterion;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
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
    private final LoadingCache<SamlRegisteredServiceCacheKey, CachedMetadataResolverResult> cache;

    private final Cache<SamlRegisteredServiceCacheKey, String> failures;

    private final Duration failureCacheExpiration;

    @Getter
    private final OpenSamlConfigBean openSamlConfigBean;

    public SamlRegisteredServiceDefaultCachingMetadataResolver(
        final CasConfigurationProperties casProperties,
        final CacheLoader<SamlRegisteredServiceCacheKey, CachedMetadataResolverResult> loader,
        final OpenSamlConfigBean openSamlConfigBean) {

        this.openSamlConfigBean = openSamlConfigBean;

        val core = casProperties.getAuthn().getSamlIdp().getMetadata().getCore();
        val metadataCacheExpiration = Beans.newDuration(core.getCacheExpiration());
        this.cache = Caffeine.newBuilder()
            .maximumSize(core.getCacheMaximumSize())
            .recordStats()
            .executor(task -> Thread.ofVirtual().name("saml-metadata-cache").start(task))
            .expireAfter(new SamlRegisteredServiceMetadataExpirationPolicy(metadataCacheExpiration))
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
        val cacheResult = loadMetadataResolver(service, cacheKey);
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
     * reload per request.
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
        val criteria = new EvaluableEntityRoleEntityDescriptorCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME);
        return Unchecked.supplier(() -> Iterables.size(cachedResult.getMetadataResolver().resolve(new CriteriaSet(criteria))) == 1).get();
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
