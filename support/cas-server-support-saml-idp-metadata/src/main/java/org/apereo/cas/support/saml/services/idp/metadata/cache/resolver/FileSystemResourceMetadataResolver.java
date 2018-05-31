package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import lombok.extern.slf4j.Slf4j;
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
import org.springframework.core.io.FileSystemResource;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This is {@link FileSystemResourceMetadataResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class FileSystemResourceMetadataResolver extends BaseSamlRegisteredServiceMetadataResolver {
    public FileSystemResourceMetadataResolver(final SamlIdPProperties samlIdPProperties,
                                              final OpenSamlConfigBean configBean) {
        super(samlIdPProperties, configBean);
    }

    @Override
    public Collection<MetadataResolver> resolve(final SamlRegisteredService service) {
        try {
            final var metadataLocation = service.getMetadataLocation();
            LOGGER.info("Loading SAML metadata from [{}]", metadataLocation);
            final var metadataResource = ResourceUtils.getResourceFrom(metadataLocation);

            final var metadataFile = metadataResource.getFile();
            final AbstractMetadataResolver metadataResolver;
            if (metadataFile.isDirectory()) {
                metadataResolver = new LocalDynamicMetadataResolver(new FilesystemLoadSaveManager<>(metadataFile, configBean.getParserPool()));
            } else {
                metadataResolver = new InMemoryResourceMetadataResolver(metadataResource, configBean);
            }
            configureAndInitializeSingleMetadataResolver(metadataResolver, service);
            return CollectionUtils.wrap(metadataResolver);
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
            return metadataResource instanceof FileSystemResource;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }
}
