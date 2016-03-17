package org.jasig.cas.support.saml.services.idp.metadata.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import org.jasig.cas.support.saml.services.SamlRegisteredService;
import org.opensaml.saml.metadata.resolver.ChainingMetadataResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * An adaptation of metadata resolver which handles the resolution of metadata resources
 * inside a Guava cache. It basically is a fancy wrapper around Guava, and constructs the cache
 * semantics before processing the resolution of metadata for a SAML service.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Component("defaultSamlRegisteredServiceCachingMetadataResolver")
public class DefaultSamlRegisteredServiceCachingMetadataResolver implements SamlRegisteredServiceCachingMetadataResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSamlRegisteredServiceCachingMetadataResolver.class);

    @Value("${cas.samlidp.metadata.cache.exp.minutes:30}")
    private long metadataCacheExpirationMinutes;

    @Autowired
    @Qualifier("chainingMetadataResolverCacheLoader")
    private ChainingMetadataResolverCacheLoader chainingMetadataResolverCacheLoader;

    private LoadingCache<SamlRegisteredService, ChainingMetadataResolver> cache;

    /**
     * Instantiates a new Saml registered service caching metadata resolver.
     */
    public DefaultSamlRegisteredServiceCachingMetadataResolver() {}

    @PostConstruct
    private void init() {
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
            throw new IllegalArgumentException("Metadata resolver could not be located from metadata " + service.getMetadataLocation(), e);
        } finally {
            if (resolver != null) {
                LOGGER.debug("Loaded and cached SAML metadata [{}] from [{}] for [{}] minute(s)",
                        resolver.getId(),
                        service.getMetadataLocation(),
                        this.metadataCacheExpirationMinutes);
            }
        }
    }
}
