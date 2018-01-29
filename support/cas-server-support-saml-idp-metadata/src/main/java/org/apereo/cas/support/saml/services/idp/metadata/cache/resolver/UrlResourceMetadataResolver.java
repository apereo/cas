package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.CanReadFileFilter;
import org.apache.commons.io.filefilter.CanWriteFileFilter;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.configuration.model.support.saml.idp.metadata.SamlIdPMetadataProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.http.HttpClient;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.FileBackedHTTPMetadataResolver;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.UrlResource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

/**
 * This is {@link UrlResourceMetadataResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class UrlResourceMetadataResolver extends BaseSamlRegisteredServiceMetadataResolver {
    private final HttpClient httpClient;

    private File metadataBackupDirectory;

    @SneakyThrows
    public UrlResourceMetadataResolver(final SamlIdPProperties samlIdPProperties,
                                       final OpenSamlConfigBean configBean,
                                       final HttpClient httpClient) {
        super(samlIdPProperties, configBean);

        this.httpClient = httpClient;
        final SamlIdPMetadataProperties md = samlIdPProperties.getMetadata();
        this.metadataBackupDirectory = new File(md.getLocation().getFile(), "metadata-backups");
        try {
            FileUtils.forceMkdir(this.metadataBackupDirectory);
        } catch (final Exception e) {
            LOGGER.error("Unable to create metadata backup directory [{}] to store downloaded metadata. "
                + "This is likely due to a permission issue", this.metadataBackupDirectory);
            LOGGER.debug(e.getMessage(), e);
        }
    }

    @Override
    public Collection<MetadataResolver> resolve(final SamlRegisteredService service) {
        try {
            final String metadataLocation = service.getMetadataLocation();
            LOGGER.info("Loading SAML metadata from [{}]", metadataLocation);
            final AbstractResource metadataResource = ResourceUtils.getResourceFrom(metadataLocation);

            final File backupFile = getMetadataBackupFile(metadataResource, service);
            final String canonicalPath = backupFile.getCanonicalPath();
            LOGGER.debug("Metadata backup file will be at [{}]", canonicalPath);
            FileUtils.forceMkdirParent(backupFile);

            cleanUpExpiredBackupMetadataFilesFor(metadataResource, service);

            final FileBackedHTTPMetadataResolver metadataProvider = new FileBackedHTTPMetadataResolver(
                this.httpClient.getWrappedHttpClient(), metadataResource.getURL().toExternalForm(),
                canonicalPath);
            configureAndInitializeSingleMetadataResolver(metadataProvider, service);
            return CollectionUtils.wrap(metadataProvider);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ArrayList<>(0);
    }

    private void cleanUpExpiredBackupMetadataFilesFor(final AbstractResource metadataResource, final SamlRegisteredService service) {
        final String prefix = getBackupMetadataFilenamePrefix(metadataResource, service);
        final Collection<File> backups = FileUtils.listFiles(this.metadataBackupDirectory,
            new AndFileFilter(CollectionUtils.wrapList(new PrefixFileFilter(prefix, IOCase.INSENSITIVE),
                new SuffixFileFilter(".xml", IOCase.INSENSITIVE),
                CanWriteFileFilter.CAN_WRITE, CanReadFileFilter.CAN_READ)), TrueFileFilter.INSTANCE);
        backups.forEach(file -> {
            try {
                FileUtils.forceDelete(file);
            } catch (final Exception e) {
                LOGGER.warn("Unable to delete metadata backup file [{}]", file);
                LOGGER.debug(e.getMessage(), e);
            }
        });
    }

    private File getMetadataBackupFile(final AbstractResource metadataResource,
                                       final RegisteredService service) throws IOException {

        LOGGER.debug("Metadata backup directory is at [{}]", this.metadataBackupDirectory.getCanonicalPath());

        final String metadataFileName = getBackupMetadataFilenamePrefix(metadataResource, service)
            .concat(getBackupMetadataFilenameSuffix(metadataResource, service));

        final File backupFile = new File(this.metadataBackupDirectory, metadataFileName);
        if (backupFile.exists()) {
            LOGGER.warn("Metadata file designated for service [{}] already exists at path [{}].", service.getName(), backupFile.getCanonicalPath());
        } else {
            LOGGER.debug("Metadata to fetch for service [{}] will be placed at [{}]", service.getName(), backupFile.getCanonicalPath());
        }
        return backupFile;
    }

    private String getBackupMetadataFilenameSuffix(final AbstractResource metadataResource, final RegisteredService service) {
        return UUID.randomUUID().toString() + ".xml";
    }

    private String getBackupMetadataFilenamePrefix(final AbstractResource metadataResource, final RegisteredService service) {
        return service.getName()
            .concat("-")
            .concat(String.valueOf(service.getId()))
            .concat("-")
            .concat(metadataResource.getFilename())
            .concat("-");
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
