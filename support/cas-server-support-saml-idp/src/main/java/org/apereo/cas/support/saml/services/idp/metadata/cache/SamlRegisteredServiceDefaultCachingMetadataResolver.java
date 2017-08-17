package org.apereo.cas.support.saml.services.idp.metadata.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An adaptation of metadata resolver which handles the resolution of metadata resources
 * inside a cache. It basically is a fancy wrapper around a cache, and constructs the cache
 * semantics before processing the resolution of metadata for a SAML service.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class SamlRegisteredServiceDefaultCachingMetadataResolver implements SamlRegisteredServiceCachingMetadataResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(SamlRegisteredServiceDefaultCachingMetadataResolver.class);
    private static final int MAX_CACHE_SIZE = 10_000;

    private final long metadataCacheExpirationMinutes;
    private final SamlRegisteredServiceMetadataResolverCacheLoader chainingMetadataResolverCacheLoader;
    private final LoadingCache<SamlRegisteredService, MetadataResolver> cache;

    public SamlRegisteredServiceDefaultCachingMetadataResolver(final long metadataCacheExpirationMinutes,
                                                               final SamlRegisteredServiceMetadataResolverCacheLoader loader) {
        this.metadataCacheExpirationMinutes = metadataCacheExpirationMinutes;
        this.chainingMetadataResolverCacheLoader = loader;
        this.cache = Caffeine.newBuilder()
                .maximumSize(MAX_CACHE_SIZE)
                .expireAfter(new SamlRegisteredServiceMetadataExpirationPolicy(metadataCacheExpirationMinutes))
                .build(this.chainingMetadataResolverCacheLoader);
    }

    @Override
    public MetadataResolver resolve(final SamlRegisteredService service) {
        MetadataResolver resolver = null;
        try {
            LOGGER.debug("Resolving metadata for [{}] at [{}].", service.getName(), service.getMetadataLocation());
            resolver = this.cache.get(service);
            return resolver;
        } catch (final Exception e) {
            throw new IllegalArgumentException("Metadata resolver could not be located from metadata "
                    + service.getMetadataLocation(), e);
        } finally {
            if (resolver != null) {
                LOGGER.debug("Loaded and cached SAML metadata [{}] from [{}]",
                        resolver.getId(),
                        service.getMetadataLocation());
            }
        }
    }
}
