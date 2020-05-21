package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.saml.InMemoryResourceMetadataResolver;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.HttpRequestUtils;
import org.apereo.cas.util.HttpUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
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
import org.apache.http.util.EntityUtils;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.AbstractMetadataResolver;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;

/**
 * This is {@link UrlResourceMetadataResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class UrlResourceMetadataResolver extends BaseSamlRegisteredServiceMetadataResolver {
    private static final String FILENAME_EXTENSION_XML = ".xml";

    private static final String DIRNAME_METADATA_BACKUPS = "metadata-backups";

    private final File metadataBackupDirectory;

    @SneakyThrows
    public UrlResourceMetadataResolver(final SamlIdPProperties samlIdPProperties,
                                       final OpenSamlConfigBean configBean) {
        super(samlIdPProperties, configBean);

        val md = samlIdPProperties.getMetadata();
        val location = SpringExpressionLanguageValueResolver.getInstance().resolve(md.getLocation());
        this.metadataBackupDirectory = new File(ResourceUtils.getRawResourceFrom(location).getFile(), DIRNAME_METADATA_BACKUPS);
        try {
            LOGGER.trace("Creating metadata backup directory at [{}]", this.metadataBackupDirectory);
            FileUtils.forceMkdir(this.metadataBackupDirectory);
        } catch (final Exception e) {
            LOGGER.error("Unable to create metadata backup directory [{}] to store downloaded metadata. "
                + "This is likely due to a permission issue", this.metadataBackupDirectory);
            LOGGER.debug(e.getMessage(), e);
        }
    }

    @Override
    public Collection<? extends MetadataResolver> resolve(final SamlRegisteredService service, final CriteriaSet criteriaSet) {
        HttpResponse response = null;
        try {
            val metadataLocation = getMetadataLocationForService(service, criteriaSet);
            LOGGER.info("Loading SAML metadata from [{}]", metadataLocation);
            val metadataResource = new UrlResource(metadataLocation);

            val backupFile = getMetadataBackupFile(metadataResource, service);
            if (backupFile.exists() && samlIdPProperties.getMetadata().isForceMetadataRefresh()) {
                cleanUpExpiredBackupMetadataFilesFor(metadataResource, service);
            }
            val canonicalPath = backupFile.getCanonicalPath();
            LOGGER.debug("Metadata backup file will be at [{}]", canonicalPath);
            FileUtils.forceMkdirParent(backupFile);

            response = fetchMetadata(metadataLocation, criteriaSet, backupFile);
            val status = HttpStatus.valueOf(response.getStatusLine().getStatusCode());
            if (shouldHttpResponseStatusBeProcessed(status)) {
                val metadataProvider = getMetadataResolverFromResponse(response, backupFile);
                configureAndInitializeSingleMetadataResolver(metadataProvider, service);
                return CollectionUtils.wrap(metadataProvider);
            }
        } catch (final UnauthorizedServiceException e) {
            LOGGER.error(e.getMessage(), e);
            throw new SamlException(e.getMessage(), e);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            HttpUtils.close(response);
        }
        return new ArrayList<>(0);
    }

    @Override
    public boolean supports(final SamlRegisteredService service) {
        try {
            val metadataLocation = getMetadataLocationForService(service, new CriteriaSet());
            return StringUtils.isNotBlank(metadataLocation)
                && StringUtils.startsWith(metadataLocation, "http")
                && !SamlUtils.isDynamicMetadataQueryConfigured(metadataLocation);
        } catch (final Exception e) {
            LOGGER.trace(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean isAvailable(final SamlRegisteredService service) {
        if (supports(service)) {
            val metadataLocation = SpringExpressionLanguageValueResolver.getInstance().resolve(service.getMetadataLocation());
            val status = HttpRequestUtils.pingUrl(metadataLocation);
            return !status.isError();
        }
        return false;
    }

    private void cleanUpExpiredBackupMetadataFilesFor(final AbstractResource metadataResource, final SamlRegisteredService service) {
        val prefix = getBackupMetadataFilenamePrefix(metadataResource, service);
        val backups = FileUtils.listFiles(this.metadataBackupDirectory,
            new AndFileFilter(CollectionUtils.wrapList(new PrefixFileFilter(prefix, IOCase.INSENSITIVE),
                new SuffixFileFilter(FILENAME_EXTENSION_XML, IOCase.INSENSITIVE),
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
     * Should http response status be processed?
     *
     * @param status the status
     * @return true/false
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
        val entity = response.getEntity();
        val result = IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8);
        val path = backupFile.toPath();
        LOGGER.trace("Writing metadata to file at [{}]", path);
        try (val output = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            IOUtils.write(result, output);
            output.flush();
        }
        EntityUtils.consume(entity);
        return new InMemoryResourceMetadataResolver(backupFile, configBean);
    }

    /**
     * Fetch metadata http response.
     *
     * @param metadataLocation the metadata location
     * @param criteriaSet      the criteria set
     * @param backupFile       the backup file
     * @return the http response
     */
    protected HttpResponse fetchMetadata(final String metadataLocation, final CriteriaSet criteriaSet, final File backupFile) {
        LOGGER.debug("Fetching metadata from [{}]", metadataLocation);
        return HttpUtils.executeGet(metadataLocation, new LinkedHashMap<>(0));
    }

    /**
     * Gets metadata location for service.
     *
     * @param service     the service
     * @param criteriaSet the criteria set
     * @return the metadata location for service
     */
    protected String getMetadataLocationForService(final SamlRegisteredService service, final CriteriaSet criteriaSet) {
        return SpringExpressionLanguageValueResolver.getInstance().resolve(service.getMetadataLocation());
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
                                         final SamlRegisteredService service) throws IOException {

        LOGGER.debug("Metadata backup directory is at [{}]", this.metadataBackupDirectory.getCanonicalPath());
        val metadataFileName = getBackupMetadataFilenamePrefix(metadataResource, service).concat(FILENAME_EXTENSION_XML);
        val backupFile = new File(this.metadataBackupDirectory, metadataFileName);
        if (backupFile.exists()) {
            LOGGER.info("Metadata file designated for service [{}] already exists at path [{}].", service.getName(), backupFile.getCanonicalPath());
        } else {
            LOGGER.debug("Metadata to fetch for service [{}] will be placed at [{}]", service.getName(), backupFile.getCanonicalPath());
        }
        return backupFile;
    }

    /**
     * Gets backup metadata filename prefix.
     * <p>
     * The metadata source may be an aggregate, representing more than on entity id
     * inside the single registered service definition. Therefor, using the service's
     * name or id may not be appropriate choice as compounds in the metadata file name.
     *
     * @param metadataResource the metadata resource
     * @param service          the service
     * @return the backup metadata filename prefix
     */
    @SneakyThrows
    protected String getBackupMetadataFilenamePrefix(final AbstractResource metadataResource, final SamlRegisteredService service) {
        val mdFileName = metadataResource.getFilename();
        if (StringUtils.isBlank(mdFileName)) {
            throw new FileNotFoundException("Unable to determine filename for " + metadataResource);
        }
        val metadataLocation = SpringExpressionLanguageValueResolver.getInstance().resolve(service.getMetadataLocation());
        val fileName = SamlUtils.isDynamicMetadataQueryConfigured(metadataLocation)
            ? service.getServiceId()
            : metadataLocation;

        val sha = DigestUtils.sha(fileName);
        LOGGER.trace("Metadata backup file for metadata location [{}] is linked to [{}]", fileName, sha);
        return sha;
    }
}
