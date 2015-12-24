package org.jasig.cas.support.saml.services.idp.metadata;

import com.google.common.cache.CacheLoader;
import net.shibboleth.utilities.java.support.component.ComponentInitializationException;
import org.jasig.cas.support.saml.OpenSamlConfigBean;
import org.jasig.cas.util.ApplicationContextProvider;
import org.opensaml.saml.metadata.resolver.ChainingMetadataResolver;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.DOMMetadataResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
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
public final class ChainingMetadataResolverCacheLoader extends CacheLoader<Resource, ChainingMetadataResolver> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChainingMetadataResolverCacheLoader.class);

    private final transient Object lock = new Object();

    @Override
    public ChainingMetadataResolver load(final Resource key) throws Exception {
        final OpenSamlConfigBean configBean = ApplicationContextProvider.getApplicationContext().getBean(OpenSamlConfigBean.class);
        try (final InputStream in = key.getInputStream()) {
            LOGGER.debug("Parsing [{}]", key.getFilename());
            final Document document = configBean.getParserPool().parse(in);
            final List<MetadataResolver> resolvers = buildSingleMetadataResolver(document, key);
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

    private List<MetadataResolver> buildSingleMetadataResolver(final Document document, final Resource key) throws IOException {
        final OpenSamlConfigBean configBean = ApplicationContextProvider.getApplicationContext().getBean(OpenSamlConfigBean.class);

        final List<MetadataResolver> resolvers = new ArrayList<>();
        final Element metadataRoot = document.getDocumentElement();
        final DOMMetadataResolver metadataProvider = new DOMMetadataResolver(metadataRoot);

        metadataProvider.setParserPool(configBean.getParserPool());
        metadataProvider.setFailFastInitialization(true);
        metadataProvider.setRequireValidMetadata(true);
        metadataProvider.setId(metadataProvider.getClass().getCanonicalName());
        LOGGER.debug("Initializing metadata resolver for [{}]", key.getURL());

        try {
            metadataProvider.initialize();
        } catch (final ComponentInitializationException ex) {
            LOGGER.warn("Could not initialize metadata resolver. Resource will be ignored", ex);
        }
        resolvers.add(metadataProvider);
        return resolvers;
    }
}


