package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.CanReadFileFilter;
import org.apache.commons.io.filefilter.CanWriteFileFilter;
import org.apache.commons.io.filefilter.PrefixFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.saml.InMemoryResourceMetadataResolver;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.HttpUtils;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.AbstractMetadataResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.UUID;

/**
 * This is {@link UrlResourceMetadataResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class UrlResourceMetadataResolver extends BaseSamlRegisteredServiceMetadataResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(UrlResourceMetadataResolver.class);

    private File metadataBackupDirectory;

    public UrlResourceMetadataResolver(final SamlIdPProperties samlIdPProperties,
                                       final OpenSamlConfigBean configBean) throws Exception {
        super(samlIdPProperties, configBean);

        final SamlIdPProperties.Metadata md = samlIdPProperties.getMetadata();
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
            final String metadataLocation = getMetadataLocationForService(service);
            LOGGER.info("Loading SAML metadata from [{}]", metadataLocation);
            final UrlResource metadataResource = new UrlResource(metadataLocation);

            final File backupFile = getMetadataBackupFile(metadataResource, service);
            final String canonicalPath = backupFile.getCanonicalPath();
            LOGGER.debug("Metadata backup file will be at [{}]", canonicalPath);
            FileUtils.forceMkdirParent(backupFile);

            final HttpResponse response = fetchMetadata(metadataLocation);
            cleanUpExpiredBackupMetadataFilesFor(metadataResource, service);
            if (response != null) {
                final HttpStatus status = HttpStatus.valueOf(response.getStatusLine().getStatusCode());
                if (shouldHttpResponseStatusBeProcessed(status)) {
                    final AbstractMetadataResolver metadataProvider = getMetadataResolverFromResponse(response, backupFile);
                    buildSingleMetadataResolver(metadataProvider, service);
                    return CollectionUtils.wrap(metadataProvider);
                }
            }


        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return new ArrayList<>(0);
    }

    /**
     * Should http response status be processed?
     *
     * @param status the status
     * @return the boolean
     */
    protected boolean shouldHttpResponseStatusBeProcessed(final HttpStatus status) {
        return status.is2xxSuccessful();
    }

    /**
     * Gets metadata resolver from response.
     *
     * @param response   the response
     * @param backupFile the backup file
     * @return the metadata resolver from response
     * @throws Exception the exception
     */
    protected AbstractMetadataResolver getMetadataResolverFromResponse(final HttpResponse response, final File backupFile) throws Exception {
        final String result = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
        try (FileWriter output = new FileWriter(backupFile)) {
            IOUtils.write(result, output);
            output.flush();
        }
        return new InMemoryResourceMetadataResolver(backupFile, configBean);
    }

    /**
     * Fetch metadata http response.
     *
     * @param metadataLocation the metadata location
     * @return the http response
     */
    protected HttpResponse fetchMetadata(final String metadataLocation) {
        return HttpUtils.executeGet(metadataLocation, new LinkedHashMap<>());
    }

    /**
     * Gets metadata location for service.
     *
     * @param service the service
     * @return the metadata location for service
     */
    protected String getMetadataLocationForService(final SamlRegisteredService service) {
        return service.getMetadataLocation();
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

    /**
     * Gets metadata backup file.
     *
     * @param metadataResource the metadata resource
     * @param service          the service
     * @return the metadata backup file
     * @throws IOException the io exception
     */
    protected File getMetadataBackupFile(final AbstractResource metadataResource,
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
            final String metadataLocation = getMetadataLocationForService(service);
            return StringUtils.isNotBlank(metadataLocation) && StringUtils.startsWith(metadataLocation, "http");
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }
}
