package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.services.RegisteredServiceAccessStrategyUtils;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.saml.InMemoryResourceMetadataResolver;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpRequestUtils;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.util.spring.SpringExpressionLanguageValueResolver;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
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
import org.apache.commons.lang3.Strings;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.jooq.lambda.Unchecked;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.AbstractMetadataResolver;
import org.springframework.core.io.AbstractResource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

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

    private final HttpClient httpClient;

    private final File metadataBackupDirectory;

    public UrlResourceMetadataResolver(final HttpClient httpClient,
                                       final SamlIdPProperties samlIdPProperties,
                                       final OpenSamlConfigBean configBean) {
        super(samlIdPProperties, configBean);
        this.httpClient = httpClient;

        val md = samlIdPProperties.getMetadata();
        val backupLocation = StringUtils.defaultIfBlank(md.getHttp().getMetadataBackupLocation(), md.getFileSystem().getLocation());
        val location = SpringExpressionLanguageValueResolver.getInstance().resolve(backupLocation);
        this.metadataBackupDirectory = FunctionUtils.doUnchecked(
            () -> new File(ResourceUtils.getRawResourceFrom(location).getFile(), DIRNAME_METADATA_BACKUPS));
        FunctionUtils.doAndHandle(file -> {
            LOGGER.trace("Creating metadata backup directory at [{}]", file);
            FileUtils.forceMkdir(file);
        }, e -> {
            LOGGER.error("Unable to create metadata backup directory [{}] to store downloaded metadata. "
                + "This is likely due to a permission issue", metadataBackupDirectory);
            LOGGER.debug(e.getMessage(), e);
            return metadataBackupDirectory;
        }).accept(metadataBackupDirectory);
    }

    @Audit(action = AuditableActions.SAML2_METADATA_RESOLUTION,
        actionResolverName = AuditActionResolvers.SAML2_METADATA_RESOLUTION_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.SAML2_METADATA_RESOLUTION_RESOURCE_RESOLVER)
    @Override
    public Collection<? extends MetadataResolver> resolve(final SamlRegisteredService service, final CriteriaSet criteriaSet) {
        HttpResponse response = null;
        try {
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service);
            val metadataLocations = getMetadataLocationsForService(service, criteriaSet);

            for (val metadataLocation : metadataLocations) {
                LOGGER.info("Loading SAML metadata from [{}]", metadataLocations);
                val metadataResource = new UrlResource(metadataLocation);

                val backupFile = getMetadataBackupFile(metadataResource, service);
                if (backupFile.exists() && samlIdPProperties.getMetadata().getHttp().isForceMetadataRefresh()) {
                    LOGGER.debug("CAS is configured to forcefully refresh metadata for service [{}]. Old metadata backup files "
                        + "will now be deleted for this service.", service.getName());
                    cleanUpExpiredBackupMetadataFilesFor(metadataResource, service);
                }
                val canonicalPath = backupFile.getCanonicalPath();
                LOGGER.debug("Metadata backup file for [{}] will be at [{}]", service.getName(), canonicalPath);
                FileUtils.forceMkdirParent(backupFile);

                if (backupFile.exists() && backupFile.canRead()) {
                    try {
                        val metadataProvider = getMetadataResolverFromFile(backupFile);
                        configureAndInitializeSingleMetadataResolver(metadataProvider, service);
                        if (Boolean.TRUE.equals(metadataProvider.isRootValid())) {
                            LOGGER.debug("Metadata backup file for service [{}] at [{}] is valid. CAS will reuse the SAML2 metadata file "
                                    + "at [{}] and will not download new metadata from [{}]",
                                service.getName(), canonicalPath, canonicalPath, metadataLocation);
                            return CollectionUtils.wrap(metadataProvider);
                        }
                    } catch (final Exception e) {
                        LoggingUtils.error(LOGGER, e);
                    }

                    LOGGER.info("Metadata backup file found for service [{}] at [{}] is invalid and will be disregarded. "
                            + "CAS will proceed to download new metadata from [{}]",
                        service.getName(), canonicalPath, metadataLocation);
                    FileUtils.forceDelete(backupFile);

                }

                response = fetchMetadata(service, metadataLocation, criteriaSet, backupFile);
                val status = response != null ? HttpStatus.valueOf(response.getCode()) : HttpStatus.BAD_REQUEST;
                LOGGER.debug("Received metadata response status code [{}]", status);
                if (shouldHttpResponseStatusBeProcessed(status)) {
                    val metadataProvider = getMetadataResolverFromResponse(response, backupFile);
                    configureAndInitializeSingleMetadataResolver(metadataProvider, service);
                    return CollectionUtils.wrap(metadataProvider);
                }
            }
        } catch (final UnauthorizedServiceException e) {
            LoggingUtils.error(LOGGER, e);
            throw new SamlException(e.getMessage(), e);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }
        return new ArrayList<>();
    }

    @Override
    public boolean supports(final SamlRegisteredService service) {
        try {
            val metadataLocations = getMetadataLocationsForService(service, new CriteriaSet());
            return metadataLocations
                .stream()
                .anyMatch(metadataLocation ->
                    StringUtils.isNotBlank(metadataLocation)
                        && Strings.CI.startsWith(metadataLocation, "http")
                        && !SamlUtils.isDynamicMetadataQueryConfigured(metadataLocation));
        } catch (final Exception e) {
            LOGGER.trace(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean isAvailable(final SamlRegisteredService service) {
        if (supports(service)) {
            val locations = org.springframework.util.StringUtils.commaDelimitedListToSet(
                SpringExpressionLanguageValueResolver.getInstance().resolve(service.getMetadataLocation()));
            return locations
                .stream()
                .map(metadataLocation -> StringUtils.substringBefore(metadataLocation, "/entities/{0}"))
                .anyMatch(metadataLocation -> {
                    val status = HttpRequestUtils.pingUrl(metadataLocation);
                    return !status.isError();
                });
        }
        return false;
    }

    protected boolean shouldHttpResponseStatusBeProcessed(final HttpStatus status) {
        return status.is2xxSuccessful();
    }

    protected AbstractMetadataResolver getMetadataResolverFromResponse(final HttpResponse response,
                                                                       final File backupFile) throws Exception {
        val entity = ((HttpEntityContainer) response).getEntity();
        val result = IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8);
        val path = backupFile.toPath();
        LOGGER.trace("Writing metadata to file at [{}]", path);
        try (val output = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            IOUtils.write(result, output);
            output.flush();
        }
        EntityUtils.consume(entity);
        return getMetadataResolverFromFile(backupFile);
    }

    private InMemoryResourceMetadataResolver getMetadataResolverFromFile(final File backupFile) throws Exception {
        val metadataResolver = new InMemoryResourceMetadataResolver(backupFile, configBean);
        metadataResolver.setId("RegisteredServiceMetadata-" + backupFile.getName());
        return metadataResolver;
    }

    protected HttpResponse fetchMetadata(final SamlRegisteredService service,
                                         final String metadataLocation,
                                         final CriteriaSet criteriaSet,
                                         final File backupFile) {
        LOGGER.debug("Fetching metadata from [{}]", metadataLocation);
        val exec = HttpExecutionRequest.builder()
            .method(HttpMethod.GET)
            .url(metadataLocation)
            .proxyUrl(service.getMetadataProxyLocation())
            .httpClient(httpClient)
            .maximumRetryAttempts(samlIdPProperties.getMetadata().getCore().getMaximumRetryAttempts())
            .build();
        return HttpUtils.execute(exec);
    }

    protected Set<String> getMetadataLocationsForService(final SamlRegisteredService service, final CriteriaSet criteriaSet) {
        return org.springframework.util.StringUtils.commaDelimitedListToSet(
            SpringExpressionLanguageValueResolver.getInstance().resolve(service.getMetadataLocation()));
    }

    protected File getMetadataBackupFile(final AbstractResource metadataResource,
                                         final SamlRegisteredService service) throws IOException {

        LOGGER.debug("Metadata backup directory is at [{}]", this.metadataBackupDirectory.getCanonicalPath());
        val metadataFileName = getBackupMetadataFilenamePrefix(metadataResource, service).concat(FILENAME_EXTENSION_XML);
        val backupFile = new File(metadataBackupDirectory, metadataFileName);
        if (backupFile.exists()) {
            LOGGER.info("Metadata file designated for service [{}] already exists at path [{}].", service.getName(), backupFile.getCanonicalPath());
        } else {
            LOGGER.debug("Metadata to fetch for service [{}] will be placed at [{}]", service.getName(), backupFile.getCanonicalPath());
        }
        return backupFile;
    }

    protected String getBackupMetadataFilenamePrefix(final AbstractResource metadataResource, final SamlRegisteredService service) {
        val metadataLocation = SpringExpressionLanguageValueResolver.getInstance().resolve(service.getMetadataLocation());
        val fileName = SamlUtils.isDynamicMetadataQueryConfigured(metadataLocation)
            ? service.getServiceId()
            : metadataLocation;

        val sha = DigestUtils.sha(fileName);
        LOGGER.trace("Metadata backup file for metadata location [{}] is linked to [{}]", fileName, sha);
        return sha;
    }

    private void cleanUpExpiredBackupMetadataFilesFor(final AbstractResource metadataResource,
                                                      final SamlRegisteredService service) {
        val prefix = getBackupMetadataFilenamePrefix(metadataResource, service);
        val backups = FileUtils.listFiles(this.metadataBackupDirectory,
            new AndFileFilter(CollectionUtils.wrapList(new PrefixFileFilter(prefix, IOCase.INSENSITIVE),
                new SuffixFileFilter(FILENAME_EXTENSION_XML, IOCase.INSENSITIVE),
                CanWriteFileFilter.CAN_WRITE, CanReadFileFilter.CAN_READ)), TrueFileFilter.INSTANCE);
        backups.forEach(Unchecked.consumer(FileUtils::forceDelete));
    }
}
