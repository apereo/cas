package org.jasig.cas.support.saml.services.idp.metadata.cache;

import com.google.common.cache.CacheLoader;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import org.jasig.cas.support.saml.OpenSamlConfigBean;
import org.jasig.cas.util.ApplicationContextProvider;
import org.jasig.cas.util.ResourceUtils;
import org.opensaml.saml.metadata.resolver.ChainingMetadataResolver;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.DOMMetadataResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.AbstractResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link ChainingMetadataResolverCacheLoader} that uses Guava's cache loading strategy
 * to keep track of metadata resources and resolvers.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
public final class ChainingMetadataResolverCacheLoader extends CacheLoader<String, ChainingMetadataResolver> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChainingMetadataResolverCacheLoader.class);

    private final transient Object lock = new Object();

    @Override
    public ChainingMetadataResolver load(final String metadataLocation) throws Exception {
        final OpenSamlConfigBean configBean = ApplicationContextProvider.getApplicationContext().getBean(OpenSamlConfigBean.class);
        LOGGER.info("Loading SAML metadata from {}", metadataLocation);

        final AbstractResource metadataResource = ResourceUtils.getResourceFrom(metadataLocation);
        try (final InputStream in = metadataResource.getInputStream()) {
            LOGGER.debug("Parsing metadata from [{}]", metadataLocation);
            final Document document = configBean.getParserPool().parse(in);
            final List<MetadataResolver> resolvers = buildSingleMetadataResolver(document, metadataLocation);
            final ChainingMetadataResolver metadataResolver = new ChainingMetadataResolver();
            synchronized (this.lock) {
                metadataResolver.setId(ChainingMetadataResolver.class.getCanonicalName());
                metadataResolver.setResolvers(resolvers);
                metadataResolver.initialize();

            }
            return metadataResolver;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<MetadataResolver> buildSingleMetadataResolver(final Document document, final String key) throws IOException {
        try {
            final OpenSamlConfigBean configBean = ApplicationContextProvider.getApplicationContext().getBean(OpenSamlConfigBean.class);

            final List<MetadataResolver> resolvers = new ArrayList<>();
            final Element metadataRoot = document.getDocumentElement();
            final DOMMetadataResolver metadataProvider = new DOMMetadataResolver(metadataRoot);

            metadataProvider.setParserPool(configBean.getParserPool());
            metadataProvider.setFailFastInitialization(true);
            metadataProvider.setRequireValidMetadata(true);
            metadataProvider.setId(metadataProvider.getClass().getCanonicalName());
            LOGGER.debug("Initializing metadata resolver from [{}]", key);

            metadataProvider.initialize();
            resolvers.add(metadataProvider);
            return resolvers;
        } catch (final ComponentInitializationException ex) {
            throw new RuntimeException("Could not initialize metadata resolver. Resource will be ignored", ex);
        }
    }
}


