package org.apereo.cas.support.saml.services.idp.metadata.cache.resolver;

import module java.base;
import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.configuration.model.support.saml.idp.SamlIdPProperties;
import org.apereo.cas.configuration.support.Beans;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.jspecify.annotations.Nullable;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.metadata.resolver.impl.AbstractMetadataResolver;
import org.springframework.http.HttpHeaders;
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

    private static final String FILENAME_EXTENSION_VALIDATORS = ".properties";

    private static final String VALIDATOR_DIGEST = "digest";

    private static final String VALIDATOR_ETAG = "etag";

    private static final String VALIDATOR_LAST_MODIFIED = "last-modified";

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
            return resolveMetadataLocations(service, criteriaSet, getMetadataLocationsForService(service, criteriaSet));
        } catch (final UnauthorizedServiceException e) {
            LoggingUtils.error(LOGGER, e);
            throw new SamlException(e.getMessage(), e);
        } catch (final SamlException e) {
            throw e;
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            throw new SamlException("Unable to load SAML2 metadata for service [%s]: %s".formatted(service.getName(), e.getMessage()), e);
        }
    }

    /**
     * Resolves metadata from the locations of a service.
     * <p>
     * The metadata backup file is the last known good copy of the remote document and is the
     * only thing standing between a metadata host that is momentarily unreachable and an
     * outage for the service. It is therefore never removed ahead of a download attempt:
     * {@code force-metadata-refresh} means "do not serve the backup without checking the
     * remote first", not "destroy the backup", and a successful download overwrites it in
     * place. Every location is tried before the backup is used, since the locations of one
     * service are mirrors of one document and a live mirror is better than a stale copy.
     * When nothing can be loaded, the reason each location failed is reported in the exception,
     * which is what the metadata cache remembers and what the operator gets to see.
     *
     * @param service           the registered service
     * @param criteriaSet       the criteria set
     * @param metadataLocations the metadata locations, tried in order
     * @return the resolvers
     * @throws Exception the exception, carrying the reason each location failed
     */
    protected Collection<? extends MetadataResolver> resolveMetadataLocations(
        final SamlRegisteredService service,
        final CriteriaSet criteriaSet,
        final Collection<String> metadataLocations)
            throws Exception {
        val backupFile = getMetadataBackupFile(service, criteriaSet);
        val canonicalPath = backupFile.getCanonicalPath();
        LOGGER.debug("Metadata backup file for [{}] will be at [{}]", service.getName(), canonicalPath);
        FileUtils.forceMkdirParent(backupFile);

        if (!samlIdPProperties.getMetadata().getHttp().isForceMetadataRefresh()) {
            val backupResolver = buildMetadataResolverFromBackupFile(service, backupFile, true);
            if (backupResolver != null) {
                LOGGER.debug("Metadata backup file for service [{}] at [{}] is valid. CAS will reuse the SAML2 metadata file "
                        + "and will not download new metadata from [{}]", service.getName(), canonicalPath, metadataLocations);
                return CollectionUtils.wrap(backupResolver);
            }
        }

        val failures = new ArrayList<String>();
        for (val metadataLocation : metadataLocations) {
            val metadataResolver = resolveMetadataLocation(service, criteriaSet, metadataLocation, backupFile, failures);
            if (metadataResolver != null) {
                return CollectionUtils.wrap(metadataResolver);
            }
        }

        val fallbackResolver = buildMetadataResolverFromBackupFile(service, backupFile, false);
        if (fallbackResolver != null) {
            val age = getMetadataBackupAge(backupFile);
            if (isMetadataBackupStale(age)) {
                LOGGER.error("Unable to download SAML2 metadata for service [{}]: [{}]. CAS will fall back onto the metadata backup "
                        + "file at [{}], which its source last confirmed [{}] ago, longer than the allowed [{}]. The metadata is likely "
                        + "out of date; restore access to the metadata source.", service.getName(), failures, canonicalPath, age,
                    samlIdPProperties.getMetadata().getHttp().getMetadataBackupMaxAge());
            } else {
                LOGGER.warn("Unable to download SAML2 metadata for service [{}]: [{}]. CAS will fall back onto the metadata backup "
                    + "file at [{}], which its source last confirmed [{}] ago.", service.getName(), failures, canonicalPath, age);
            }
            return CollectionUtils.wrap(fallbackResolver);
        }
        throw new SamlException("Unable to load SAML2 metadata for service [%s] and no metadata backup file is available: %s"
            .formatted(service.getName(), failures));
    }

    /**
     * Resolves metadata from a single location. A {@code 304 Not Modified} answer to the conditional
     * request is served from the backup file the request was conditioned on.
     *
     * @param service          the registered service
     * @param criteriaSet      the criteria set
     * @param metadataLocation the metadata location
     * @param backupFile       the metadata backup file
     * @param failures         collects the reason this location failed, if it does
     * @return the resolver, or null when this location cannot produce metadata
     */
    protected @Nullable AbstractMetadataResolver resolveMetadataLocation(final SamlRegisteredService service,
                                                                         final CriteriaSet criteriaSet,
                                                                         final String metadataLocation,
                                                                         final File backupFile,
                                                                         final List<String> failures) {
        LOGGER.info("Loading SAML metadata from [{}]", metadataLocation);
        HttpResponse response = null;
        try {
            response = fetchMetadata(service, metadataLocation, criteriaSet, backupFile);
            if (response == null) {
                failures.add("%s: no response".formatted(metadataLocation));
                return null;
            }
            val status = HttpStatus.resolve(response.getCode());
            if (status == HttpStatus.NOT_MODIFIED) {
                val backupResolver = buildMetadataResolverFromBackupFile(service, backupFile, false);
                if (backupResolver != null) {
                    LOGGER.debug("Metadata at [{}] is not modified; CAS will use the metadata backup file at [{}]", metadataLocation, backupFile);
                    Files.setLastModifiedTime(backupFile.toPath(), FileTime.from(Instant.now(Clock.systemUTC())));
                    return backupResolver;
                }
                failures.add("%s: not modified, but the metadata backup file cannot be used".formatted(metadataLocation));
                return null;
            }
            if (status != null && status.is2xxSuccessful()) {
                val metadataProvider = getMetadataResolverFromResponse(response, backupFile);
                configureAndInitializeSingleMetadataResolver(metadataProvider, service);
                return metadataProvider;
            }
            val reason = "%s: HTTP %s %s".formatted(metadataLocation, response.getCode(),
                StringUtils.defaultString(response.getReasonPhrase())).trim();
            LOGGER.warn("Unable to download SAML2 metadata for service [{}] from [{}]", service.getName(), reason);
            failures.add(reason);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
            failures.add("%s: %s".formatted(metadataLocation, StringUtils.defaultIfBlank(e.getMessage(), e.getClass().getSimpleName())));
        } finally {
            HttpUtils.close(response);
        }
        return null;
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
                val backupFile = getMetadataBackupFile(service, new CriteriaSet());
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
    
    /**
     * Builds the metadata resolver from the downloaded document and stores the document as the backup file.
     * The resolver is built from the document in memory, never by reading the backup file back: other resolutions
     * may replace that file at any moment. The document stays in bytes throughout, the form it was downloaded in,
     * which is what the parser, the backup file and the validators digest all take: decoding it into a string
     * would double its size in memory, and encoding it back would copy it again.
     *
     * @param response   the response
     * @param backupFile the metadata backup file
     * @return the metadata resolver
     * @throws Exception the exception
     */
    protected AbstractMetadataResolver getMetadataResolverFromResponse(final HttpResponse response,
                                                                       final File backupFile)
            throws Exception {
        val entity = ((HttpEntityContainer) response).getEntity();
        val metadata = EntityUtils.toByteArray(entity);
        writeMetadataToBackupFile(metadata, backupFile);
        writeMetadataValidators(response, metadata, backupFile);
        EntityUtils.consume(entity);
        val metadataResolver = new InMemoryResourceMetadataResolver(new ByteArrayInputStream(metadata), configBean);
        metadataResolver.setId("RegisteredServiceMetadata-" + backupFile.getName());
        return metadataResolver;
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
    protected void writeMetadataToBackupFile(final byte[] metadata, final File backupFile) throws Exception {
        writeFileAtomically(metadata, backupFile);
    }

    /**
     * Records the validators of a downloaded metadata document next to its backup file, so that the next download can
     * be a conditional request. The validators carry a digest of the document they belong to and are only used while
     * the backup file still holds that document: services sharing a location share the backup file, and one of them
     * may have replaced it with a newer document while another wrote its validators.
     *
     * @param response   the response that carried the document
     * @param metadata   the metadata document
     * @param backupFile the metadata backup file
     * @throws Exception the exception
     */
    protected void writeMetadataValidators(final HttpResponse response, final byte[] metadata, final File backupFile) throws Exception {
        val validatorsFile = getMetadataValidatorsFile(backupFile);
        val etag = response.getFirstHeader(HttpHeaders.ETAG);
        val lastModified = response.getFirstHeader(HttpHeaders.LAST_MODIFIED);
        if (etag == null && lastModified == null) {
            Files.deleteIfExists(validatorsFile.toPath());
            return;
        }
        val validators = new Properties();
        validators.setProperty(VALIDATOR_DIGEST, DigestUtils.sha256(metadata));
        if (etag != null) {
            validators.setProperty(VALIDATOR_ETAG, etag.getValue());
        }
        if (lastModified != null) {
            validators.setProperty(VALIDATOR_LAST_MODIFIED, lastModified.getValue());
        }
        try (val writer = new StringWriter()) {
            validators.store(writer, null);
            writeFileAtomically(writer.toString().getBytes(StandardCharsets.UTF_8), validatorsFile);
        }
    }

    /**
     * Gets the headers that make the download of a metadata location conditional on the backup file having changed:
     * {@code If-None-Match} and {@code If-Modified-Since}, from the validators recorded with the backup file. Nothing
     * is returned when there is no backup file, no validators or validators recorded for a different document.
     *
     * @param backupFile the metadata backup file
     * @return the request headers
     */
    protected Map<String, String> getConditionalRequestHeaders(final File backupFile) {
        val validatorsFile = getMetadataValidatorsFile(backupFile);
        if (!backupFile.canRead() || !validatorsFile.canRead()) {
            return Map.of();
        }
        try (val reader = Files.newBufferedReader(validatorsFile.toPath(), StandardCharsets.UTF_8)) {
            val validators = new Properties();
            validators.load(reader);
            val digest = DigestUtils.sha256(Files.readAllBytes(backupFile.toPath()));
            if (!digest.equals(validators.getProperty(VALIDATOR_DIGEST))) {
                LOGGER.debug("Validators at [{}] belong to another metadata document than [{}]", validatorsFile, backupFile);
                return Map.of();
            }
            val headers = new LinkedHashMap<String, String>();
            val etag = validators.getProperty(VALIDATOR_ETAG);
            if (StringUtils.isNotBlank(etag)) {
                headers.put(HttpHeaders.IF_NONE_MATCH, etag);
            }
            val lastModified = validators.getProperty(VALIDATOR_LAST_MODIFIED);
            if (StringUtils.isNotBlank(lastModified)) {
                headers.put(HttpHeaders.IF_MODIFIED_SINCE, lastModified);
            }
            return headers;
        } catch (final Exception e) {
            LOGGER.debug("Unable to read metadata validators at [{}]: [{}]", validatorsFile, e.getMessage());
            return Map.of();
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Reports the metadata backup file of the service: where it is, and how long ago its source last confirmed it,
     * either by sending it or by answering that it has not changed. A backup older than
     * {@code metadata-backup-max-age} is reported as stale.
     */
    @Override
    public Map<String, Object> describe(final SamlRegisteredService service, final CriteriaSet criteriaSet) {
        return FunctionUtils.<Map<String, Object>>doAndHandle(() -> {
            val backupFile = getMetadataBackupFile(service, criteriaSet);
            val details = new LinkedHashMap<String, Object>();
            details.put("metadataBackupFile", backupFile.getCanonicalPath());
            details.put("metadataBackupExists", backupFile.exists());
            if (backupFile.exists()) {
                val age = getMetadataBackupAge(backupFile);
                details.put("metadataBackupLastConfirmed", Instant.ofEpochMilli(backupFile.lastModified()).toString());
                details.put("metadataBackupAge", age.toString());
                details.put("metadataBackupStale", isMetadataBackupStale(age));
                details.put("metadataValidators", getMetadataValidatorsFile(backupFile).exists());
            }
            return details;
        }, e -> Map.<String, Object>of()).get();
    }

    protected Duration getMetadataBackupAge(final File backupFile) {
        return Duration.between(Instant.ofEpochMilli(backupFile.lastModified()), Instant.now(Clock.systemUTC()));
    }

    protected boolean isMetadataBackupStale(final Duration age) {
        val maxAge = Beans.newDuration(samlIdPProperties.getMetadata().getHttp().getMetadataBackupMaxAge());
        return !maxAge.isNegative() && !maxAge.isZero() && age.compareTo(maxAge) > 0;
    }

    protected File getMetadataValidatorsFile(final File backupFile) {
        return new File(backupFile.getParentFile(), backupFile.getName() + FILENAME_EXTENSION_VALIDATORS);
    }

    private static void writeFileAtomically(final byte[] content, final File file) throws Exception {
        val target = file.toPath();
        LOGGER.trace("Writing file at [{}]", target);
        val temporaryFile = Files.createTempFile(target.getParent(), file.getName(), ".tmp");
        try {
            Files.write(temporaryFile, content);
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
            .headers(new LinkedHashMap<>(getConditionalRequestHeaders(backupFile)))
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
     * metadata location or, for a metadata query, by the entity id being queried, and by nothing
     * else, so no resource needs to be resolved to find it.
     *
     * @param service     the registered service
     * @param criteriaSet the criteria set carrying the requested entity id, if any
     * @return the backup file, which may not exist yet
     * @throws IOException the exception
     */
    protected File getMetadataBackupFile(final SamlRegisteredService service, final CriteriaSet criteriaSet) throws IOException {
        LOGGER.debug("Metadata backup directory is at [{}]", this.metadataBackupDirectory.getCanonicalPath());
        val metadataFileName = getBackupMetadataFilenamePrefix(service, criteriaSet).concat(FILENAME_EXTENSION_XML);
        val backupFile = new File(metadataBackupDirectory, metadataFileName);
        LOGGER.debug("Metadata backup file for service [{}] is [{}] (exists: [{}])", service.getName(),
            backupFile.getCanonicalPath(), backupFile.exists());
        return backupFile;
    }

    /**
     * Gets the name of the metadata backup file, without extension. A metadata query answers with one entity per
     * request, so its backup is named after the requested entity id, and after the service id only when the request
     * names no entity; the service id of a metadata query service is normally a pattern shared by every entity.
     *
     * @param service     the registered service
     * @param criteriaSet the criteria set carrying the requested entity id, if any
     * @return the file name prefix
     */
    protected String getBackupMetadataFilenamePrefix(final SamlRegisteredService service, final CriteriaSet criteriaSet) {
        val metadataLocation = SpringExpressionLanguageValueResolver.getInstance().resolve(service.getMetadataLocation());
        val fileName = SamlUtils.isDynamicMetadataQueryConfigured(metadataLocation)
            ? getEntityIdFromCriteriaSet(criteriaSet).orElseGet(service::getServiceId)
            : metadataLocation;

        val sha = DigestUtils.sha(fileName);
        LOGGER.trace("Metadata backup file for metadata location [{}] is linked to [{}]", fileName, sha);
        return sha;
    }
}
