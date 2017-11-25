package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.ScriptingUtils;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.AbstractResource;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This is {@link GroovyResourceMetadataResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class GroovyResourceMetadataResolver extends BaseSamlRegisteredServiceMetadataResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroovyResourceMetadataResolver.class);

    public GroovyResourceMetadataResolver(final SamlIdPProperties samlIdPProperties,
                                          final OpenSamlConfigBean configBean) {
        super(samlIdPProperties, configBean);
    }

    @Override
    public Collection<MetadataResolver> resolve(final SamlRegisteredService service) {
        try {
            final String metadataLocation = service.getMetadataLocation();
            LOGGER.info("Loading SAML metadata via [{}]", metadataLocation);
            final AbstractResource metadataResource = ResourceUtils.getResourceFrom(metadataLocation);
            final Object[] args = {service, this.configBean, this.samlIdPProperties, LOGGER};
            final MetadataResolver metadataResolver =
                ScriptingUtils.executeGroovyScript(metadataResource, args, MetadataResolver.class);
            if (metadataResolver != null) {
                return CollectionUtils.wrap(metadataResolver);
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ArrayList<>(0);
    }

    @Override
    public boolean supports(final SamlRegisteredService service) {
        final String metadataLocation = service.getMetadataLocation();
        return ScriptingUtils.isExternalGroovyScript(metadataLocation);
    }
}
