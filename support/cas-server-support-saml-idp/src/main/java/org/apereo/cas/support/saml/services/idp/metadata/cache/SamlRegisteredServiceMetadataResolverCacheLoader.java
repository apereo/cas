package org.apereo.cas.support.saml.services.idp.metadata.cache;

import com.github.benmanes.caffeine.cache.CacheLoader;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.cache.resolver.SamlRegisteredServiceMetadataResolver;
import org.apereo.cas.util.http.HttpClient;
import org.opensaml.saml.metadata.resolver.ChainingMetadataResolver;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This is {@link SamlRegisteredServiceMetadataResolverCacheLoader} that uses Guava's cache loading strategy
 * to keep track of metadata resources and resolvers. The cache loader here supports loading
 * metadata resources from SAML services, supports dynamic metadata queries and is able
 * to run various validation filters on the metadata before finally caching the resolver.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class SamlRegisteredServiceMetadataResolverCacheLoader implements CacheLoader<SamlRegisteredService, MetadataResolver> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SamlRegisteredServiceMetadataResolverCacheLoader.class);

    /**
     * The Config bean.
     */
    protected OpenSamlConfigBean configBean;

    /**
     * The Http client.
     */
    protected HttpClient httpClient;
    private final transient Object lock = new Object();
    private final Collection<SamlRegisteredServiceMetadataResolver> availableResolvers;
    private final SamlIdPProperties samlIdPProperties;

    public SamlRegisteredServiceMetadataResolverCacheLoader(final OpenSamlConfigBean configBean,
                                                            final HttpClient httpClient,
                                                            final SamlIdPProperties samlIdPProperties,
                                                            final Collection<SamlRegisteredServiceMetadataResolver> availableResolvers) {
        this.configBean = configBean;
        this.httpClient = httpClient;
        this.samlIdPProperties = samlIdPProperties;
        this.availableResolvers = availableResolvers;
    }

    @Override
    public ChainingMetadataResolver load(final SamlRegisteredService service) {
        try {
            final ChainingMetadataResolver metadataResolver = new ChainingMetadataResolver();
            final List<MetadataResolver> metadataResolvers = new ArrayList<>();
            
            this.availableResolvers.stream()
                    .filter(r -> r.supports(service))
                    .map(r -> r.resolve(service))
                    .forEach(metadataResolvers::addAll);

            if (metadataResolvers.isEmpty()) {
                throw new SamlException("No metadata resolvers could be configured for service " + service.getName()
                        + " with metadata location " + service.getMetadataLocation());
            }

            synchronized (this.lock) {
                metadataResolver.setId(ChainingMetadataResolver.class.getCanonicalName());
                metadataResolver.setResolvers(metadataResolvers);
                metadataResolver.initialize();
            }
            return metadataResolver;
        } catch (final Exception e) {
            throw new SamlException(e.getMessage(), e);
        }
    }
}


