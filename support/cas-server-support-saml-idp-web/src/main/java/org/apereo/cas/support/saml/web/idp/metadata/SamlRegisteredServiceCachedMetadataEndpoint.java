package org.apereo.cas.support.saml.web.idp.metadata;

import org.apereo.cas.audit.AuditableContext;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.SamlException;
import org.apereo.cas.support.saml.SamlUtils;
import org.apereo.cas.support.saml.services.SamlRegisteredService;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.web.BaseCasActuatorEndpoint;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.shibboleth.shared.resolver.CriteriaSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jooq.lambda.Unchecked;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.core.criterion.SatisfyAnyCriterion;
import org.opensaml.saml.criterion.EntityRoleCriterion;
import org.opensaml.saml.metadata.criteria.entity.impl.EvaluableEntityRoleEntityDescriptorCriterion;
import org.opensaml.saml.saml2.common.TimeBoundSAMLObject;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * This is {@link SamlRegisteredServiceCachedMetadataEndpoint}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Slf4j
@RestControllerEndpoint(id = "samlIdPRegisteredServiceMetadataCache", enableByDefault = false)
public class SamlRegisteredServiceCachedMetadataEndpoint extends BaseCasActuatorEndpoint {
    private final SamlRegisteredServiceCachingMetadataResolver cachingMetadataResolver;

    private final ServicesManager servicesManager;

    private final AuditableExecution registeredServiceAccessStrategyEnforcer;

    private final OpenSamlConfigBean openSamlConfigBean;

    public SamlRegisteredServiceCachedMetadataEndpoint(final CasConfigurationProperties casProperties,
                                                       final SamlRegisteredServiceCachingMetadataResolver cachingMetadataResolver,
                                                       final ServicesManager servicesManager,
                                                       final AuditableExecution registeredServiceAccessStrategyEnforcer,
                                                       final OpenSamlConfigBean openSamlConfigBean) {
        super(casProperties);
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
     */
    @DeleteMapping
    @Operation(summary = "Invalidate SAML2 metadata cache using a service id or entity id. The service id could be the registered service numeric identifier, its name or actual service id. "
                         + "In case the service definition points to an aggregate, you may also specify an entity id to locate the service provider within that aggregate. "
                         + "If you do not specify any parameters, all entries in the metadata cache will be invalidated.",
        parameters = {
            @Parameter(name = "serviceId"),
            @Parameter(name = "entityId")
        })
    public ResponseEntity invalidate(
        @Nullable
        @RequestParam(required = false)
        final String serviceId,
        @Nullable
        @RequestParam(required = false)
        final String entityId) {

        if (StringUtils.isBlank(serviceId)) {
            cachingMetadataResolver.invalidate();
            LOGGER.info("Cleared SAML2 registered service metadata cache");
            return ResponseEntity.noContent().build();
        }
        val registeredService = findRegisteredService(serviceId);
        val criteriaSet = new CriteriaSet();
        val effectiveEntityId = StringUtils.defaultIfBlank(entityId, registeredService.getServiceId());
        criteriaSet.add(new EntityIdCriterion(effectiveEntityId));
        criteriaSet.add(new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME));
        cachingMetadataResolver.invalidate(registeredService, criteriaSet);
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
     * @return the cached metadata object
     */
    @GetMapping(produces = {
        MEDIA_TYPE_SPRING_BOOT_V2_JSON,
        MEDIA_TYPE_SPRING_BOOT_V3_JSON,
        MediaType.APPLICATION_FORM_URLENCODED_VALUE,
        MediaType.APPLICATION_JSON_VALUE,
        MEDIA_TYPE_CAS_YAML
    })
    @Operation(summary = "Get SAML2 cached metadata for a SAML2 registered service. The service id could be the registered service numeric identifier, its name or actual service id. "
                         + "In case the service definition points to an aggregate, you may also specify an entity id to locate the service provider within that aggregate",
        parameters = {
            @Parameter(name = "serviceId", required = true),
            @Parameter(name = "entityId")
        })
    public ResponseEntity<? extends Map> getCachedMetadataObject(
        @RequestParam
        final String serviceId,
        @Nullable
        @RequestParam(required = false)
        final String entityId,
        @RequestParam(required = false, defaultValue = "true")
        final boolean force) {

        return FunctionUtils.doAndHandle(() -> {
            val registeredService = findRegisteredService(serviceId);
            val criteriaSet = new CriteriaSet();
            if (StringUtils.isNotBlank(entityId)) {
                criteriaSet.add(new EntityIdCriterion(entityId));
                criteriaSet.add(new EntityRoleCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME));
            } else {
                criteriaSet.add(new EvaluableEntityRoleEntityDescriptorCriterion(SPSSODescriptor.DEFAULT_ELEMENT_NAME));
                criteriaSet.add(new SatisfyAnyCriterion(true));
            }

            val metadataResolverResult = force
                ? Optional.of(cachingMetadataResolver.resolve(registeredService, criteriaSet))
                : cachingMetadataResolver.getIfPresent(registeredService, criteriaSet);
            return metadataResolverResult.map(Unchecked.function(result -> {
                val iteration = result.getMetadataResolver().resolve(criteriaSet).spliterator();
                val body = StreamSupport.stream(iteration, false)
                    .filter(TimeBoundSAMLObject::isValid)
                    .map(entity -> {
                        val details = CollectionUtils.wrap(
                            "cachedInstant", result.getCachedInstant(),
                            "metadata", SamlUtils.transformSamlObject(openSamlConfigBean, entity).toString());
                        return Pair.of(entity.getEntityID(), details);
                    })
                    .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
                return ResponseEntity.ok(body);
            })).orElseThrow(() -> new SamlException("Unable to locate and resolve metadata for service " + registeredService.getName()));
        }, e -> ResponseEntity.badRequest().body(Map.of("error", e.getMessage()))).get();
    }

    private SamlRegisteredService findRegisteredService(final String serviceId) {
        var matchedServices = (Collection<RegisteredService>) null;
        if (NumberUtils.isCreatable(serviceId)) {
            val id = Long.parseLong(serviceId);
            matchedServices = List.of(servicesManager.findServiceBy(id, SamlRegisteredService.class));
        } else {
            matchedServices = servicesManager.findServiceBy(svc -> svc instanceof SamlRegisteredService
                                                                   && (svc.getName().equalsIgnoreCase(serviceId) || svc.getServiceId().equalsIgnoreCase(serviceId)));
        }
        if (matchedServices.isEmpty()) {
            throw new IllegalArgumentException("Unable to locate service " + serviceId);
        }
        val registeredService = (SamlRegisteredService) matchedServices.iterator().next();
        val ctx = AuditableContext.builder()
            .registeredService(registeredService)
            .build();
        val result = this.registeredServiceAccessStrategyEnforcer.execute(ctx);
        result.throwExceptionIfNeeded();
        LOGGER.debug("Located registered service definition [{}]", registeredService);
        return registeredService;
    }
}
