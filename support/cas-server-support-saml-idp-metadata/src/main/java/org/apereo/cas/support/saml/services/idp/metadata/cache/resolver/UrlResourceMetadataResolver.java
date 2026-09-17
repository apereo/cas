package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import module java.base;
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
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.jspecify.annotations.Nullable;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.AbstractMetadataResolver;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

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

    /**
     * The CAS HTTP client, carrying the deployment's TLS trust store and hostname verifier.
     */
    protected final HttpClient httpClient;

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
        try {
            RegisteredServiceAccessStrategyUtils.ensureServiceAccessIsAllowed(service);
            val metadataLocations = getMetadataLocationsForService(service, criteriaSet);

            for (val metadataLocation : metadataLocations) {
                val resolvers = resolveMetadataLocation(service, criteriaSet, metadataLocation);
                if (!resolvers.isEmpty()) {
                    return resolvers;
                }
            }
        } catch (final UnauthorizedServiceException e) {
            LoggingUtils.error(LOGGER, e);
            throw new SamlException(e.getMessage(), e);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return new ArrayList<>();
    }

    /**
     * Resolves metadata from a single location.
     * <p>
     * The metadata backup file is the last known good copy of the remote document and is the
     * only thing standing between a metadata host that is momentarily unreachable and an
     * outage for the service. It is therefore never removed ahead of a download attempt:
     * {@code force-metadata-refresh} means "do not serve the backup without checking the
     * remote first", not "destroy the backup", and a successful download overwrites it in
     * place. When the download cannot be completed, the backup is used instead.
     *
     * @param service          the registered service
     * @param criteriaSet      the criteria set
     * @param metadataLocation the metadata location
     * @return the resolvers, or an empty collection when neither the remote document nor the
     *     backup file can produce valid metadata
     * @throws Exception the exception
     */
    protected Collection<? extends MetadataResolver> resolveMetadataLocation(
        final SamlRegisteredService service,
        final CriteriaSet criteriaSet,
        final String metadataLocation) throws Exception {

        LOGGER.info("Loading SAML metadata from [{}]", metadataLocation);
        val backupFile = getMetadataBackupFile(service);
        val canonicalPath = backupFile.getCanonicalPath();
        LOGGER.debug("Metadata backup file for [{}] will be at [{}]", service.getName(), canonicalPath);
        FileUtils.forceMkdirParent(backupFile);

        if (!samlIdPProperties.getMetadata().getHttp().isForceMetadataRefresh()) {
            val backupResolver = buildMetadataResolverFromBackupFile(service, backupFile, true);
            if (backupResolver != null) {
                LOGGER.debug("Metadata backup file for service [{}] at [{}] is valid. CAS will reuse the SAML2 metadata file "
                        + "at [{}] and will not download new metadata from [{}]",
                    service.getName(), canonicalPath, canonicalPath, metadataLocation);
                return CollectionUtils.wrap(backupResolver);
            }
        }

        HttpResponse response = null;
        try {
            response = fetchMetadata(service, metadataLocation, criteriaSet, backupFile);
            val status = response != null ? HttpStatus.resolve(response.getCode()) : HttpStatus.BAD_REQUEST;
            LOGGER.debug("Received metadata response status code [{}]", status);
            if (status != null && shouldHttpResponseStatusBeProcessed(status)) {
                val metadataProvider = getMetadataResolverFromResponse(response, backupFile);
                configureAndInitializeSingleMetadataResolver(metadataProvider, service);
                return CollectionUtils.wrap(metadataProvider);
            }
        } catch (final UnauthorizedServiceException e) {
            throw e;
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        } finally {
            HttpUtils.close(response);
        }

        val fallbackResolver = buildMetadataResolverFromBackupFile(service, backupFile, false);
        if (fallbackResolver != null) {
            LOGGER.warn("Unable to download SAML2 metadata for service [{}] from [{}]. CAS will fall back onto "
                    + "the last known metadata backup file at [{}], which may be out of date.",
                service.getName(), metadataLocation, canonicalPath);
            return CollectionUtils.wrap(fallbackResolver);
        }
        return new ArrayList<>();
    }

    /**
     * Builds a metadata resolver from the metadata backup file, when that file exists and is
     * able to produce metadata. The file is left in place in every case, including when it
     * turns out to be unusable, so that it stays available to later attempts; a successful
     * download replaces its contents.
     * <p>
     * The two callers want different answers about root validity. Deciding to serve the
     * backup <i>instead of</i> contacting the remote source requires the root to be positively
     * valid. Falling back onto it <i>because</i> the remote source could not be reached only
     * requires that the root is not known to be invalid, since a document that carries no
     * {@code validUntil} reports its validity as unknown and is the common case; expired
     * descriptors within it are still filtered out by {@code require-valid-metadata}.
     *
     * @param service          the registered service
     * @param backupFile       the metadata backup file
     * @param requireValidRoot whether the root of the document must be positively valid
     * @return the resolver, or null when the backup is absent or cannot be used
     */
    protected @Nullable AbstractMetadataResolver buildMetadataResolverFromBackupFile(final SamlRegisteredService service,
                                                                                     final File backupFile,
                                                                                     final boolean requireValidRoot) {
        if (backupFile.exists() && backupFile.canRead()) {
            try {
                val metadataProvider = getMetadataResolverFromFile(backupFile);
                configureAndInitializeSingleMetadataResolver(metadataProvider, service);
                val rootValid = metadataProvider.isRootValid();
                val acceptable = requireValidRoot
                    ? Boolean.TRUE.equals(rootValid)
                    : !Boolean.FALSE.equals(rootValid);
                if (acceptable) {
                    return metadataProvider;
                }
                LOGGER.info("Metadata backup file for service [{}] at [{}] is no longer valid and will be disregarded.",
                    service.getName(), backupFile);
            } catch (final Exception e) {
                LoggingUtils.error(LOGGER, e);
            }
        }
        return null;
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
            return FunctionUtils.doAndHandle(() -> {
                val backupFile = getMetadataBackupFile(service);
                if (backupFile.exists() && backupFile.canRead()) {
                    LOGGER.trace("Metadata for service [{}] is available locally at [{}]", service.getName(), backupFile);
                    return Boolean.TRUE;
                }
                val locations = org.springframework.util.StringUtils.commaDelimitedListToSet(
                    SpringExpressionLanguageValueResolver.getInstance().resolve(service.getMetadataLocation()));
                return locations
                    .stream()
                    .map(metadataLocation -> StringUtils.substringBefore(metadataLocation, "/entities/{0}"))
                    .anyMatch(metadataLocation -> !HttpRequestUtils.pingUrl(metadataLocation).isError());
            }, throwable -> Boolean.FALSE).get();
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
        writeMetadataToBackupFile(result, backupFile);
        EntityUtils.consume(entity);
        return getMetadataResolverFromFile(backupFile);
    }

    /**
     * Replaces the contents of the metadata backup file in a single step.
     * <p>
     * The backup file caches a metadata location, not a service, so it is named after the location
     * alone and every service pointing at that location shares it. That sharing is deliberate -- one
     * federation aggregate should not be downloaded and stored once per service -- but it means one
     * resolution can be reading the file while another is replacing it, and those two are separate
     * entries in the metadata cache, so nothing upstream serializes them. Writing through a temporary
     * file in the same directory and moving it into place keeps a reader on either the whole previous
     * document or the whole new one, never on a half-written one.
     *
     * @param metadata   the metadata document to store
     * @param backupFile the metadata backup file
     * @throws Exception the exception
     */
    protected void writeMetadataToBackupFile(final String metadata, final File backupFile) throws Exception {
        val target = backupFile.toPath();
        LOGGER.trace("Writing metadata to file at [{}]", target);
        val temporaryFile = Files.createTempFile(target.getParent(), backupFile.getName(), ".tmp");
        try {
            Files.writeString(temporaryFile, metadata, StandardCharsets.UTF_8);
            try {
                Files.move(temporaryFile, target, StandardCopyOption.ATOMIC_MOVE);
            } catch (final AtomicMoveNotSupportedException e) {
                LOGGER.debug(e.getMessage(), e);
                Files.move(temporaryFile, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    private InMemoryResourceMetadataResolver getMetadataResolverFromFile(final File backupFile) throws Exception {
        val metadataResolver = new InMemoryResourceMetadataResolver(backupFile, configBean);
        metadataResolver.setId("RegisteredServiceMetadata-" + backupFile.getName());
        return metadataResolver;
    }

    protected @Nullable HttpResponse fetchMetadata(final SamlRegisteredService service,
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

    /**
     * Locates the metadata backup file for a service. The file is identified by the service's
     * metadata location, or by its entity id for a metadata query, and by nothing else, so no
     * resource needs to be resolved to find it.
     *
     * @param service the registered service
     * @return the backup file, which may not exist yet
     * @throws IOException the exception
     */
    protected File getMetadataBackupFile(final SamlRegisteredService service) throws IOException {
        LOGGER.debug("Metadata backup directory is at [{}]", this.metadataBackupDirectory.getCanonicalPath());
        val metadataFileName = getBackupMetadataFilenamePrefix(service).concat(FILENAME_EXTENSION_XML);
        val backupFile = new File(metadataBackupDirectory, metadataFileName);
        if (backupFile.exists()) {
            LOGGER.info("Metadata file designated for service [{}] already exists at path [{}].", service.getName(), backupFile.getCanonicalPath());
        } else {
            LOGGER.debug("Metadata to fetch for service [{}] will be placed at [{}]", service.getName(), backupFile.getCanonicalPath());
        }
        return backupFile;
    }

    protected String getBackupMetadataFilenamePrefix(final SamlRegisteredService service) {
        val metadataLocation = SpringExpressionLanguageValueResolver.getInstance().resolve(service.getMetadataLocation());
        val fileName = SamlUtils.isDynamicMetadataQueryConfigured(metadataLocation)
            ? service.getServiceId()
            : metadataLocation;

        val sha = DigestUtils.sha(fileName);
        LOGGER.trace("Metadata backup file for metadata location [{}] is linked to [{}]", fileName, sha);
        return sha;
    }
}
