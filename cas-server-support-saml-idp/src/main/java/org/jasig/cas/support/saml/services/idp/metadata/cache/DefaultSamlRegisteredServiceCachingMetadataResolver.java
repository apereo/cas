package org.jasig.cas.support.saml.services.idp.metadata.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import org.jasig.cas.support.saml.services.SamlRegisteredService;
import org.opensaml.saml.metadata.resolver.ChainingMetadataResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * This is {@link DefaultSamlRegisteredServiceCachingMetadataResolver}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Component("defaultSamlRegisteredServiceCachingMetadataResolver")
public final class DefaultSamlRegisteredServiceCachingMetadataResolver implements SamlRegisteredServiceCachingMetadataResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultSamlRegisteredServiceCachingMetadataResolver.class);

    @Value("${cas.samlidp.metadata.cache.exp.minutes:30}")
    private long metadataCacheExpirationMinutes;

    private final LoadingCache<String, ChainingMetadataResolver> cache;

    /**
     * Instantiates a new Saml registered service caching metadata resolver.
     */
    public DefaultSamlRegisteredServiceCachingMetadataResolver() {
        this.cache = CacheBuilder.newBuilder().maximumSize(1)
                .expireAfterWrite(this.metadataCacheExpirationMinutes, TimeUnit.MINUTES).build(new ChainingMetadataResolverCacheLoader());
    }

    @Override
    public ChainingMetadataResolver resolve(final SamlRegisteredService service) {
        try {
            final ChainingMetadataResolver resolver = this.cache.get(service.getMetadataLocation());
            this.cache.put(service.getMetadataLocation(), resolver);
            return resolver;
        } catch (final Exception e) {
            throw new IllegalArgumentException("Metadata resolver could not be located from metadata " + service.getMetadataLocation(), e);
        }

    }
}
