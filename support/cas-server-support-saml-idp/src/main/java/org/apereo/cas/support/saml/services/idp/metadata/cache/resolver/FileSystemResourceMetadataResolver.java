package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.InMemoryResourceMetadataResolver;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;
import org.opensaml.core.xml.persist.FilesystemLoadSaveManager;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.AbstractMetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.LocalDynamicMetadataResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

/**
 * This is {@link FileSystemResourceMetadataResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class FileSystemResourceMetadataResolver extends BaseSamlRegisteredServiceMetadataResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemResourceMetadataResolver.class);

    public FileSystemResourceMetadataResolver(final SamlIdPProperties samlIdPProperties,
                                              final OpenSamlConfigBean configBean) {
        super(samlIdPProperties, configBean);
    }

    @Override
    public Collection<MetadataResolver> resolve(final SamlRegisteredService service) {
        try {
            final String metadataLocation = service.getMetadataLocation();
            LOGGER.info("Loading SAML metadata from [{}]", metadataLocation);
            final AbstractResource metadataResource = ResourceUtils.getResourceFrom(metadataLocation);

            final File metadataFile = metadataResource.getFile();
            final AbstractMetadataResolver metadataResolver;
            if (metadataFile.isDirectory()) {
                metadataResolver = new LocalDynamicMetadataResolver(new FilesystemLoadSaveManager<>(metadataFile, configBean.getParserPool()));
            } else {
                metadataResolver = new InMemoryResourceMetadataResolver(metadataResource, configBean);
            }
            buildSingleMetadataResolver(metadataResolver, service);
            return CollectionUtils.wrap(metadataResolver);
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
            return metadataResource instanceof FileSystemResource;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }
}
