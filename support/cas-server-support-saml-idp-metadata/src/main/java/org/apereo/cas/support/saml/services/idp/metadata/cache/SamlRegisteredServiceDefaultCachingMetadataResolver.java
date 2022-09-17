package org.apereo.cas.support.saml.services.idp.metadata.cache;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.google.common.collect.Iterables;
import lombok.Builder;
import lombok.Getter;
import lombok.Synchronized;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import org.jooq.lambda.Unchecked;
import org.opensaml.core.criterion.SatisfyAnyCriterion;
import org.opensaml.saml.metadata.criteria.entity.impl.EvaluableEntityRoleEntityDescriptorCriterion;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;

import java.util.Objects;
import java.util.Optional;

/**
 * An adaptation of metadata resolver which handles the resolution of metadata resources
 * inside a cache. It basically is a fancy wrapper around a cache, and constructs the cache
 * semantics before processing the resolution of metadata for a SAML service.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class SamlRegisteredServiceDefaultCachingMetadataResolver implements SamlRegisteredServiceCachingMetadataResolver {

    private final LoadingCache<SamlRegisteredServiceCacheKey, CachedMetadataResolverResult> cache;

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
            .expireAfter(new SamlRegisteredServiceMetadataExpirationPolicy(metadataCacheExpiration))
            .build(loader);
    }

    private static long countResolvableEntityDescriptors(final MetadataResolutionResult result) {
        return FunctionUtils.doUnchecked(() -> {
            val criteria = new EvaluableEntityRoleEntityDescriptorCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME);
            return Iterables.size(result.getResult().getMetadataResolver().resolve(new CriteriaSet(criteria)));
        });
    }

    @Override
    @Synchronized
    public CachedMetadataResolverResult resolve(final SamlRegisteredService service, final CriteriaSet criteriaSet) {
        val metadataLocation = SpringExpressionLanguageValueResolver.getInstance().resolve(service.getMetadataLocation());
        LOGGER.debug("Resolving metadata for [{}] at [{}]", service.getName(), metadataLocation);
        val cacheKey = new SamlRegisteredServiceCacheKey(service, criteriaSet);
        LOGGER.trace("Locating cached metadata resolver using key [{}] for service [{}]", cacheKey.getId(), service.getName());
        return FunctionUtils.doAndRetry(retryContext -> {
            val queryResult = locateAndCacheMetadataResolver(service, criteriaSet, cacheKey);

            val result = isMetadataResolverAcceptable(queryResult, criteriaSet);
            if (!result.isValid()) {
                val count = countResolvableEntityDescriptors(result);
                if (count == 1) {
                    invalidate(service, criteriaSet);
                }
                LOGGER.warn("SAML metadata resolver [{}] obtained from the cache is "
                            + "unable to produce/resolve valid metadata from [{}]. Metadata resolver cache entry with key [{}] "
                            + "has been invalidated. Retry attempt: [{}]",
                    result.getResult().getMetadataResolver().getId(), metadataLocation,
                    cacheKey.getId(), retryContext.getRetryCount());
                throw new SamlException("Unable to locate a valid SAML metadata resolver for "
                                        + metadataLocation + " to locate " + criteriaSet);
            }
            return queryResult.getResult();
        });
    }

    @Override
    public void invalidate() {
        LOGGER.trace("Invalidating cache, removing all metadata resolvers");
        cache.invalidateAll();
    }

    @Override
    public void invalidate(final SamlRegisteredService service, final CriteriaSet criteriaSet) {
        LOGGER.trace("Invalidating cache for [{}].", service.getName());
        val cacheKey = new SamlRegisteredServiceCacheKey(service, criteriaSet);
        cache.invalidate(cacheKey);
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
        val md = queryResult.getEntityDescriptor()
            .orElseGet(Unchecked.supplier(() -> queryResult.getResult().getMetadataResolver().resolveSingle(criteriaSet)));

        return MetadataResolutionResult.builder()
            .valid(md != null && md.isValid())
            .entityDescriptor(Optional.ofNullable(md))
            .result(queryResult.getResult())
            .build();
    }

    protected MetadataResolverCacheQueryResult locateAndCacheMetadataResolver(
        final SamlRegisteredService service,
        final CriteriaSet criteriaSet,
        final SamlRegisteredServiceCacheKey cacheKey) {

        val result = cache
            .asMap()
            .values()
            .stream()
            .map(Unchecked.function(res -> {
                val entity = res.getMetadataResolver().resolveSingle(criteriaSet);
                return Optional.ofNullable(entity)
                    .map(e -> MetadataResolverCacheQueryResult.builder()
                        .result(res)
                        .entityDescriptor(Optional.of(e))
                        .build());
            }))
            .filter(Optional::isPresent)
            .flatMap(Optional::stream)
            .findFirst();

        if (result.isPresent()) {
            return result.get();
        }
        LOGGER.debug("Loading metadata resolver from the cache using [{}]", cacheKey.getCacheKey());
        val cacheResult = Objects.requireNonNull(cache.get(cacheKey));
        LOGGER.debug("Loaded and cached SAML metadata [{}] from [{}]",
            cacheResult.getMetadataResolver().getId(), service.getMetadataLocation());
        return MetadataResolverCacheQueryResult.builder()
            .entityDescriptor(Optional.empty())
            .result(cacheResult)
            .build();
    }

    @SuperBuilder
    @Getter
    @SuppressWarnings("UnusedMethod")
    private static class MetadataResolutionResult {
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
    private static class MetadataResolverCacheQueryResult {
        private final CachedMetadataResolverResult result;

        @Builder.Default
        private final Optional<EntityDescriptor> entityDescriptor = Optional.empty();
    }
}
