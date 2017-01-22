package org.apereo.cas.support.saml.services.idp.metadata.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.opensaml.saml.metadata.resolver.ChainingMetadataResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * An adaptation of metadata resolver which handles the resolution of metadata resources
 * inside a Guava cache. It basically is a fancy wrapper around Guava, and constructs the cache
 * semantics before processing the resolution of metadata for a SAML service.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DefaultSamlRegisteredServiceCachingMetadataResolver implements SamlRegisteredServiceCachingMetadataResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSamlRegisteredServiceCachingMetadataResolver.class);

    private long metadataCacheExpirationMinutes;

    private ChainingMetadataResolverCacheLoader chainingMetadataResolverCacheLoader;

    private LoadingCache<SamlRegisteredService, ChainingMetadataResolver> cache;

    public DefaultSamlRegisteredServiceCachingMetadataResolver(final long metadataCacheExpirationMinutes,
                                                               final ChainingMetadataResolverCacheLoader chainingMetadataResolverCacheLoader) {
        this.metadataCacheExpirationMinutes = metadataCacheExpirationMinutes;
        this.chainingMetadataResolverCacheLoader = chainingMetadataResolverCacheLoader;
        this.cache = CacheBuilder.newBuilder().maximumSize(1)
                .expireAfterWrite(this.metadataCacheExpirationMinutes, TimeUnit.MINUTES).build(this.chainingMetadataResolverCacheLoader);
    }

    @Override
    public ChainingMetadataResolver resolve(final SamlRegisteredService service) {
        ChainingMetadataResolver resolver = null;
        try {
            LOGGER.debug("Resolving metadata for [{}] at [{}].", service.getName(), service.getMetadataLocation());
            resolver = this.cache.get(service);
            return resolver;
        } catch (final Exception e) {
            throw new IllegalArgumentException("Metadata resolver could not be located from metadata "
                    + service.getMetadataLocation(), e);
        } finally {
            if (resolver != null) {
                LOGGER.debug("Loaded and cached SAML metadata [{}] from [{}] for [{}] minute(s)",
                        resolver.getId(),
                        service.getMetadataLocation(),
                        this.metadataCacheExpirationMinutes);
            }
        }
    }

    public void setChainingMetadataResolverCacheLoader(final ChainingMetadataResolverCacheLoader chainingMetadataResolverCacheLoader) {
        this.chainingMetadataResolverCacheLoader = chainingMetadataResolverCacheLoader;
    }

    public void setMetadataCacheExpirationMinutes(final long metadataCacheExpirationMinutes) {
        this.metadataCacheExpirationMinutes = metadataCacheExpirationMinutes;
    }
}
