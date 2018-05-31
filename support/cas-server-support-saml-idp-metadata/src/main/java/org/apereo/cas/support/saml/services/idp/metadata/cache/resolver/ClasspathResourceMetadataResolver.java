package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.DOMMetadataResolver;
import org.springframework.core.io.ClassPathResource;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This is {@link ClasspathResourceMetadataResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class ClasspathResourceMetadataResolver extends BaseSamlRegisteredServiceMetadataResolver {


    public ClasspathResourceMetadataResolver(final SamlIdPProperties samlIdPProperties,
                                             final OpenSamlConfigBean configBean) {
        super(samlIdPProperties, configBean);
    }

    @Override
    public Collection<MetadataResolver> resolve(final SamlRegisteredService service) {
        final var metadataLocation = service.getMetadataLocation();
        LOGGER.info("Loading SAML metadata from [{}]", metadataLocation);
        try (var in = ResourceUtils.getResourceFrom(metadataLocation).getInputStream()) {
            LOGGER.debug("Parsing metadata from [{}]", metadataLocation);
            final var document = this.configBean.getParserPool().parse(in);

            final var metadataRoot = document.getDocumentElement();
            final var metadataProvider = new DOMMetadataResolver(metadataRoot);
            configureAndInitializeSingleMetadataResolver(metadataProvider, service);
            return CollectionUtils.wrap(metadataProvider);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ArrayList<>(0);
    }

    @Override
    public boolean supports(final SamlRegisteredService service) {
        try {
            final var metadataLocation = service.getMetadataLocation();
            final var metadataResource = ResourceUtils.getResourceFrom(metadataLocation);
            return metadataResource instanceof ClassPathResource;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }
}
