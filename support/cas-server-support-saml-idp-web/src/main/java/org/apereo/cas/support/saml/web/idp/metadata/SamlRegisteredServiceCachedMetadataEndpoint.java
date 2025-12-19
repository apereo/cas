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
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.BaseCasRestActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.lambda.Unchecked;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.criterion.SatisfyAnyCriterion;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.metadata.criteria.entity.impl.EvaluableEntityRoleEntityDescriptorCriterion;
import org.opensaml.saml.saml2.common.TimeBoundSAMLObject;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.endpoint.Access;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
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
    private final ObjectProvider<@NonNull SamlRegisteredServiceCachingMetadataResolver> cachingMetadataResolver;

    private final ObjectProvider<@NonNull ServicesManager> servicesManager;

    private final ObjectProvider<@NonNull AuditableExecution> registeredServiceAccessStrategyEnforcer;

    private final ObjectProvider<@NonNull OpenSamlConfigBean> openSamlConfigBean;

    public SamlRegisteredServiceCachedMetadataEndpoint(
        final CasConfigurationProperties casProperties,
        final ObjectProvider<@NonNull SamlRegisteredServiceCachingMetadataResolver> cachingMetadataResolver,
        final ObjectProvider<@NonNull ServicesManager> servicesManager,
        final ObjectProvider<@NonNull AuditableExecution> registeredServiceAccessStrategyEnforcer,
        final ObjectProvider<@NonNull OpenSamlConfigBean> openSamlConfigBean) {
        super(casProperties, openSamlConfigBean.getObject().getApplicationContext());
        this.cachingMetadataResolver = cachingMetadataResolver;
        this.servicesManager = servicesManager;
        this.registeredServiceAccessStrategyEnforcer = registeredServiceAccessStrategyEnforcer;
        this.openSamlConfigBean = openSamlConfigBean;
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
        @RequestParam(required = false) final String entityId) throws Throwable {

        if (StringUtils.isBlank(serviceId)) {
            cachingMetadataResolver.getObject().invalidate();
            LOGGER.info("Cleared SAML2 registered service metadata cache");
            return ResponseEntity.noContent().build();
        }
        val registeredService = findRegisteredService(serviceId);
        val criteriaSet = new CriteriaSet();
        val effectiveEntityId = StringUtils.defaultIfBlank(entityId, registeredService.getServiceId());
        criteriaSet.add(new EntityIdCriterion(effectiveEntityId));
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
                            .filter(TimeBoundSAMLObject::isValid)
                            .map(entity -> {
                                val details = CollectionUtils.wrap("cachedInstant", result.getCachedInstant());
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

    protected SamlRegisteredService findRegisteredService(final String serviceId) throws Throwable {
        var matchedServices = (Collection<RegisteredService>) null;
        if (NumberUtils.isCreatable(serviceId)) {
            val id = Long.parseLong(serviceId);
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
