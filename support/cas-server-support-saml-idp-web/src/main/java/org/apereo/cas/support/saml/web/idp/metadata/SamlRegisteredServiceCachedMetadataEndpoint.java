package org.apereo.cas.support.saml.web.idp.metadata;

import module java.base;
import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.http.HttpExecutionRequest;
import org.apereo.cas.util.http.HttpUtils;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
import com.google.common.primitives.Longs;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.core5.http.HttpEntityContainer;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.jooq.lambda.Unchecked;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.criterion.SatisfyAnyCriterion;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.metadata.criteria.entity.impl.EvaluableEntityRoleEntityDescriptorCriterion;
import org.opensaml.saml.saml2.metadata.EntitiesDescriptor;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * This is {@link SamlRegisteredServiceCachedMetadataEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@Endpoint(id = "samlIdPRegisteredServiceMetadataCache", defaultAccess = Access.NONE)
public class SamlRegisteredServiceCachedMetadataEndpoint extends BaseCasRestActuatorEndpoint {
    private final ObjectProvider<SamlRegisteredServiceCachingMetadataResolver> cachingMetadataResolver;

    private final ObjectProvider<ServicesManager> servicesManager;

    private final ObjectProvider<AuditableExecution> registeredServiceAccessStrategyEnforcer;

    private final ObjectProvider<OpenSamlConfigBean> openSamlConfigBean;

    private final ObjectProvider<HttpClient> httpClient;

    public SamlRegisteredServiceCachedMetadataEndpoint(
        final CasConfigurationProperties casProperties,
        final ObjectProvider<SamlRegisteredServiceCachingMetadataResolver> cachingMetadataResolver,
        final ObjectProvider<ServicesManager> servicesManager,
        final ObjectProvider<AuditableExecution> registeredServiceAccessStrategyEnforcer,
        final ObjectProvider<OpenSamlConfigBean> openSamlConfigBean,
        final ObjectProvider<HttpClient> httpClient) {
        super(casProperties, openSamlConfigBean.getObject().getApplicationContext());
        this.cachingMetadataResolver = cachingMetadataResolver;
        this.servicesManager = servicesManager;
        this.registeredServiceAccessStrategyEnforcer = registeredServiceAccessStrategyEnforcer;
        this.openSamlConfigBean = openSamlConfigBean;
        this.httpClient = httpClient;
    }

    /**
     * Invalidate.
     *
     * @param serviceId the service id
     * @param entityId  the entity id
     * @return the response entity
     * @throws Throwable the throwable
     */
    @DeleteMapping
    @Operation(summary = "Invalidate SAML2 metadata cache using a service id or entity id. The service id could be the registered service numeric identifier, its name or actual service id. "
        + "In case the service definition points to an aggregate, you may also specify an entity id to locate the service provider within that aggregate. "
        + "If you do not specify any parameters, all entries in the metadata cache will be invalidated.",
        parameters = {
            @Parameter(name = "serviceId", description = "The service id"),
            @Parameter(name = "entityId", description = "The entity id")
        })
    public ResponseEntity invalidate(
        @Nullable
        @RequestParam(required = false) final String serviceId,
        @Nullable
        @RequestParam(required = false) final String entityId)
            throws Throwable {

        if (StringUtils.isBlank(serviceId)) {
            cachingMetadataResolver.getObject().invalidate();
            LOGGER.info("Cleared SAML2 registered service metadata cache");
            return ResponseEntity.noContent().build();
        }
        val registeredService = findRegisteredService(serviceId);
        val criteriaSet = new CriteriaSet();
        val effectiveEntityId = StringUtils.defaultIfBlank(entityId, registeredService.getServiceId());
        criteriaSet.add(new EntityIdCriterion(Objects.requireNonNull(effectiveEntityId)));
        criteriaSet.add(new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME));
        cachingMetadataResolver.getObject().invalidate(registeredService, criteriaSet);
        LOGGER.info("Invalidated SAML2 registered service metadata cache entry for [{}]", registeredService);
        return ResponseEntity.noContent()
            .header(registeredService.getClass().getSimpleName(), String.valueOf(registeredService.getId()), registeredService.getName())
            .header(EntityIdCriterion.class.getSimpleName(), effectiveEntityId)
            .build();
    }

    /**
     * Gets cached metadata object.
     *
     * @param serviceId the service id
     * @param entityId  the entity id
     * @param force     the force
     * @return the cached metadata object
     */
    @GetMapping(produces = {
        MediaType.APPLICATION_JSON_VALUE,
        MEDIA_TYPE_SPRING_BOOT_V2_JSON,
        MEDIA_TYPE_SPRING_BOOT_V3_JSON,
        MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        MEDIA_TYPE_CAS_YAML
    })
    @Operation(summary = "Get SAML2 cached metadata for a SAML2 registered service. The service id could be the registered service numeric identifier, its name or actual service id. "
        + "In case the service definition points to an aggregate, you may also specify an entity id to locate the service provider within that aggregate",
        parameters = {
            @Parameter(in = ParameterIn.QUERY, name = "serviceId", required = true, description = "The service id"),
            @Parameter(in = ParameterIn.QUERY, name = "entityId", description = "The entity id"),
            @Parameter(in = ParameterIn.QUERY, name = "includeMetadata", description = "Whether to include the XML metadata in the response")
        })
    public ResponseEntity<? extends @NonNull Map> getCachedMetadataObject(
        @RequestParam final String serviceId,
        @RequestParam(required = false, defaultValue = "true") final boolean includeMetadata,
        @Nullable
        @RequestParam(required = false) final String entityId,
        @RequestParam(required = false, defaultValue = "true") final boolean force) {

        val responseBody = new LinkedHashMap<String, Object>();
        val stopWatch = new StopWatch();
        stopWatch.start();
        try {
            return FunctionUtils.doAndHandle(() -> {
                val registeredService = findRegisteredService(serviceId);
                stopWatch.split();
                responseBody.put("registeredServiceSplitTime", stopWatch.formatSplitTime());

                val criteriaSet = new CriteriaSet();
                if (StringUtils.isNotBlank(entityId)) {
                    criteriaSet.add(new EntityIdCriterion(entityId));
                    criteriaSet.add(new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME));
                } else {
                    criteriaSet.add(new EvaluableEntityRoleEntityDescriptorCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME));
                    criteriaSet.add(new SatisfyAnyCriterion(true));
                }
                val metadataResolverResult = force
                    ? Optional.of(cachingMetadataResolver.getObject().resolve(registeredService, criteriaSet))
                    : cachingMetadataResolver.getObject().getIfPresent(registeredService, criteriaSet);
                stopWatch.split();
                responseBody.put("metadataResolverSplitTime", stopWatch.formatSplitTime());

                val resultsMap = metadataResolverResult
                    .map(Unchecked.function(result -> {
                        val iteration = result.getMetadataResolver().resolve(criteriaSet).spliterator();
                        return StreamSupport.stream(iteration, false)
                            .map(entity -> {
                                val details = CollectionUtils.<String, Object>wrap("cachedInstant", result.getCachedInstant());
                                details.put("valid", entity.isValid());
                                Optional.ofNullable(entity.getValidUntil()).ifPresent(validUntil -> details.put("validUntil", validUntil));
                                if (includeMetadata) {
                                    details.put("metadata", SamlUtils.transformSamlObject(openSamlConfigBean.getObject(), entity).toString());
                                }
                                return Pair.of(entity.getEntityID(), details);
                            })
                            .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
                    }))
                    .orElseThrow(() -> new SamlException("""
                    Unable to locate and resolve metadata for service %s. \
                    This can happen when the metadata entry is not found in the cache or \
                    the SAML2 service provider is unable to produce valid metadata via %s.
                    """.formatted(registeredService.getName(), registeredService.getMetadataLocation())));
                stopWatch.split();
                responseBody.put("metadataLoadSplitTime", stopWatch.formatSplitTime());
                responseBody.putAll(resultsMap);
                return ResponseEntity.ok(responseBody);
            }, e -> ResponseEntity.badRequest().body(Map.of("error", e.getMessage()))).get();
        } finally {
            stopWatch.stop();
        }
    }

    /**
     * Gets statistics of the metadata cache, and describes its entries.
     *
     * @return the statistics and the cached entries
     */
    @GetMapping(path = "/statistics", produces = {
        MediaType.APPLICATION_JSON_VALUE,
        MEDIA_TYPE_SPRING_BOOT_V2_JSON,
        MEDIA_TYPE_SPRING_BOOT_V3_JSON,
        MEDIA_TYPE_CAS_YAML
    })
    @Operation(summary = "Get statistics of the SAML2 metadata cache, such as its size, hit and miss counts and load times, "
        + "along with every cached entry: the service and entity it was loaded for, when it was loaded and when it expires, "
        + "where its metadata came from, and the failure remembered for it, if any.")
    public ResponseEntity<Map<String, Object>> getCacheStatistics() {
        val resolver = cachingMetadataResolver.getObject();
        val body = new LinkedHashMap<String, Object>();
        body.put("statistics", resolver.getStatistics());
        body.put("entries", resolver.getCachedEntries());
        return ResponseEntity.ok(body);
    }

    /**
     * Read the metadata of SAML2 service providers from a URL, for an application that is not registered yet. The
     * metadata is downloaded with the HTTP client the metadata resolvers use and parsed, and nothing is cached,
     * stored as a backup or registered; its signature and validity are not checked, since nothing is trusted on
     * its strength. Only http and https locations are read.
     *
     * @param metadataLocation the http or https URL of the metadata
     * @param entityId         the entity id of the service provider to read, when the metadata is an aggregate
     * @return the entity ids of the service providers the metadata holds and, when it holds one or the entity id
     * names one of them, the metadata of that service provider; or the reason the metadata could not be read
     */
    @GetMapping(path = "/preview", produces = {
        MediaType.APPLICATION_JSON_VALUE,
        MEDIA_TYPE_SPRING_BOOT_V2_JSON,
        MEDIA_TYPE_SPRING_BOOT_V3_JSON
    })
    @Operation(summary = "Read the metadata of SAML2 service providers from an http or https URL without registering, caching or storing it. "
        + "The response lists the service providers the metadata holds and, when it holds one or an entity id names one of them, "
        + "carries the metadata of that service provider",
        parameters = {
            @Parameter(in = ParameterIn.QUERY, name = "metadataLocation", required = true, description = "The http or https URL of the metadata"),
            @Parameter(in = ParameterIn.QUERY, name = "entityId", description = "The entity id of the service provider to read from an aggregate")
        })
    public ResponseEntity<Map<String, Object>> previewMetadata(
        @RequestParam final String metadataLocation,
        @Nullable
        @RequestParam(required = false) final String entityId) {
        try {
            val location = toReadableMetadataLocation(metadataLocation);
            val root = SamlUtils.transformSamlObject(openSamlConfigBean.getObject(), downloadMetadata(location), XMLObject.class);
            val providers = root != null ? findServiceProviders(root) : List.<EntityDescriptor>of();
            if (providers.isEmpty()) {
                return previewFailure("The metadata at %s holds no SAML2 service provider".formatted(location));
            }
            var selected = Optional.<EntityDescriptor>empty();
            if (StringUtils.isNotBlank(entityId)) {
                selected = providers.stream().filter(provider -> entityId.equals(provider.getEntityID())).findFirst();
                if (selected.isEmpty()) {
                    return previewFailure("The metadata at %s holds no SAML2 service provider %s".formatted(location, entityId));
                }
            } else if (providers.size() == 1) {
                selected = Optional.of(providers.getFirst());
            }
            val body = new LinkedHashMap<String, Object>();
            body.put("metadataLocation", location);
            body.put("entities", providers.stream().map(EntityDescriptor::getEntityID).filter(Objects::nonNull).toList());
            if (selected.isPresent()) {
                val provider = selected.get();
                body.put("entityId", StringUtils.defaultString(provider.getEntityID()));
                body.put("metadata", SamlUtils.transformSamlObject(openSamlConfigBean.getObject(), provider).toString());
            }
            return ResponseEntity.ok(body);
        } catch (final Exception e) {
            LoggingUtils.warn(LOGGER, e);
            return previewFailure(StringUtils.defaultIfBlank(e.getMessage(), e.getClass().getSimpleName()));
        }
    }

    protected byte[] downloadMetadata(final String location) throws Exception {
        HttpResponse response = null;
        try {
            val execution = HttpExecutionRequest.builder()
                .method(HttpMethod.GET)
                .url(location)
                .httpClient(httpClient.getObject())
                .maximumRetryAttempts(casProperties.getAuthn().getSamlIdp().getMetadata().getCore().getMaximumRetryAttempts())
                .build();
            response = HttpUtils.execute(execution);
            if (response == null) {
                throw new SamlException("%s did not answer".formatted(location));
            }
            if (!HttpStatusCode.valueOf(response.getCode()).is2xxSuccessful()) {
                throw new SamlException("%s answered HTTP %s".formatted(location, response.getCode()));
            }
            return EntityUtils.toByteArray(((HttpEntityContainer) response).getEntity());
        } finally {
            HttpUtils.close(response);
        }
    }

    private static String toReadableMetadataLocation(final String metadataLocation) {
        val uri = URI.create(metadataLocation.trim());
        if (!Strings.CI.equalsAny(uri.getScheme(), "http", "https") || StringUtils.isBlank(uri.getHost())) {
            throw new IllegalArgumentException("Metadata can only be read from an http or https URL");
        }
        return uri.toString();
    }

    private static List<EntityDescriptor> findServiceProviders(final XMLObject root) {
        if (root instanceof final EntityDescriptor entity) {
            return entity.getRoleDescriptors(SPSSODescriptor.DEFAULT_ELEMENT_NAME).isEmpty() ? List.of() : List.of(entity);
        }
        val providers = new ArrayList<EntityDescriptor>();
        if (root instanceof final EntitiesDescriptor entities) {
            entities.getEntityDescriptors().forEach(entity -> providers.addAll(findServiceProviders(entity)));
            entities.getEntitiesDescriptors().forEach(group -> providers.addAll(findServiceProviders(group)));
        }
        return providers;
    }

    private static ResponseEntity<Map<String, Object>> previewFailure(final String reason) {
        return ResponseEntity.badRequest().body(Map.of("error", reason));
    }

    protected SamlRegisteredService findRegisteredService(final String serviceId) throws Throwable {
        var matchedServices = (Collection<RegisteredService>) null;
        val id = serviceId != null ? Longs.tryParse(serviceId) : null;
        if (id != null) {
            val samlService = servicesManager.getObject().findServiceBy(id, SamlRegisteredService.class);
            matchedServices = samlService != null ? List.of(samlService) : List.of();
        } else {
            matchedServices = servicesManager.getObject().findServiceBy(svc -> svc instanceof SamlRegisteredService
                && (svc.getName().equalsIgnoreCase(serviceId) || svc.getServiceId().equalsIgnoreCase(serviceId)));
        }
        if (matchedServices.isEmpty()) {
            throw UnauthorizedServiceException.denied("Unable to locate service " + serviceId);
        }
        val registeredService = (SamlRegisteredService) matchedServices.iterator().next();
        val ctx = AuditableContext.builder()
            .registeredService(registeredService)
            .build();
        val result = registeredServiceAccessStrategyEnforcer.getObject().execute(ctx);
        result.throwExceptionIfNeeded();
        LOGGER.debug("Located registered service definition [{}]", registeredService);
        return registeredService;
    }
}
