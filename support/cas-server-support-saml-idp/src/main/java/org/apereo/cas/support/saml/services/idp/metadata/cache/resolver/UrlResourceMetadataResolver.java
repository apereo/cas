package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import org.apache.commons.io.FileUtils;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.http.HttpClient;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.FileBackedHTTPMetadataResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.UrlResource;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

/**
 * This is {@link UrlResourceMetadataResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class UrlResourceMetadataResolver extends BaseSamlRegisteredServiceMetadataResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicMetadataResolver.class);

    private final HttpClient httpClient;
    
    public UrlResourceMetadataResolver(final SamlIdPProperties samlIdPProperties,
                                       final OpenSamlConfigBean configBean,
                                       final HttpClient httpClient) {
        super(samlIdPProperties, configBean);
        this.httpClient = httpClient;
    }

    @Override
    public Collection<MetadataResolver> resolve(final SamlRegisteredService service) {
        try {

            final String metadataLocation = service.getMetadataLocation();
            LOGGER.info("Loading SAML metadata from [{}]", metadataLocation);
            final AbstractResource metadataResource = ResourceUtils.getResourceFrom(metadataLocation);
            
            final SamlIdPProperties.Metadata md = samlIdPProperties.getMetadata();
            final File backupDirectory = new File(md.getLocation().getFile(), "metadata-backups");
            final File backupFile = new File(backupDirectory, metadataResource.getFilename());

            LOGGER.debug("Metadata backup directory is designated to be [{}]", backupDirectory.getCanonicalPath());
            FileUtils.forceMkdir(backupDirectory);

            final String canonicalPath = backupFile.getCanonicalPath();
            LOGGER.debug("Metadata backup file will be at [{}]", canonicalPath);
            FileUtils.forceMkdirParent(backupFile);

            final FileBackedHTTPMetadataResolver metadataProvider = new FileBackedHTTPMetadataResolver(
                    this.httpClient.getWrappedHttpClient(), metadataResource.getURL().toExternalForm(),
                    canonicalPath);
            buildSingleMetadataResolver(metadataProvider, service);
            return CollectionUtils.wrap(metadataProvider);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    @Override
    public boolean supports(final SamlRegisteredService service) {
        try {
            final String metadataLocation = service.getMetadataLocation();
            final AbstractResource metadataResource = ResourceUtils.getResourceFrom(metadataLocation);
            return metadataResource instanceof UrlResource;
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }
}
