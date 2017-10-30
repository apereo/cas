package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.DOMMetadataResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

/**
 * This is {@link ClasspathResourceMetadataResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class ClasspathResourceMetadataResolver extends BaseSamlRegisteredServiceMetadataResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicMetadataResolver.class);

    public ClasspathResourceMetadataResolver(final SamlIdPProperties samlIdPProperties,
                                             final OpenSamlConfigBean configBean) {
        super(samlIdPProperties, configBean);
    }

    @Override
    public Collection<MetadataResolver> resolve(final SamlRegisteredService service) {
        final String metadataLocation = service.getMetadataLocation();
        LOGGER.info("Loading SAML metadata from [{}]", metadataLocation);
        try (InputStream in = ResourceUtils.getResourceFrom(metadataLocation).getInputStream()) {
            LOGGER.debug("Parsing metadata from [{}]", metadataLocation);
            final Document document = this.configBean.getParserPool().parse(in);

            final Element metadataRoot = document.getDocumentElement();
            final DOMMetadataResolver metadataProvider = new DOMMetadataResolver(metadataRoot);
            buildSingleMetadataResolver(metadataProvider, service);
            return CollectionUtils.wrap(metadataProvider);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ArrayList<>(0);
    }

    @Override
    public boolean supports(final SamlRegisteredService service) {
        try {
            final String metadataLocation = service.getMetadataLocation();
            final AbstractResource metadataResource = ResourceUtils.getResourceFrom(metadataLocation);
            return metadataResource instanceof ClassPathResource;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }
}
