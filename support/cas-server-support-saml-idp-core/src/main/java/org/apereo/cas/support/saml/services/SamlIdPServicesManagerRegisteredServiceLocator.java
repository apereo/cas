package org.apereo.cas.support.saml.services;

import module java.base;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.BaseServicesManagerRegisteredServiceLocator;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.saml.authentication.SamlIdPServiceAttributeExtractor;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceMetadataAdaptor;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.Nullable;
import org.springframework.core.Ordered;

/**
 * This is {@link SamlIdPServicesManagerRegisteredServiceLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
public class SamlIdPServicesManagerRegisteredServiceLocator extends BaseServicesManagerRegisteredServiceLocator {

    /**
     * Default maximum length of an entity id that is matched against service definitions.
     */
    public static final int DEFAULT_MAXIMUM_ENTITY_ID_LENGTH = 4096;

    private final SamlRegisteredServiceCachingMetadataResolver resolver;

    private final int maximumEntityIdLength;

    public SamlIdPServicesManagerRegisteredServiceLocator(
        final SamlRegisteredServiceCachingMetadataResolver resolver) {
        this(resolver, DEFAULT_MAXIMUM_ENTITY_ID_LENGTH);
    }

    public SamlIdPServicesManagerRegisteredServiceLocator(
        final SamlRegisteredServiceCachingMetadataResolver resolver,
        final int maximumEntityIdLength) {
        this.resolver = resolver;
        this.maximumEntityIdLength = maximumEntityIdLength;
        setOrder(Ordered.HIGHEST_PRECEDENCE);
    }

    /**
     * Extracts the entity id from the request once, since decoding and unmarshalling
     * the SAML message does not depend on the candidate, and then
     * matches candidates by entity id and metadata in evaluation order.
     */
    @Override
    public @Nullable RegisteredService locate(final Collection<? extends RegisteredService> candidates, final Service service) {
        val samlServices = candidates
            .stream()
            .filter(registeredService -> supports(registeredService, service))
            .map(SamlRegisteredService.class::cast)
            .toList();
        if (samlServices.isEmpty()) {
            return null;
        }
        return SamlIdPServiceAttributeExtractor.extract(samlServices.getFirst(), service)
            .map(pair -> {
                val attribute = pair.getLeft();
                val attributeValue = pair.getRight();
                LOGGER.trace("Located service attribute [{}] with value [{}]", attribute, attributeValue);
                return attribute.getEntityIdFrom(resolver, attributeValue);
            })
            .filter(StringUtils::isNotBlank)
            .filter(this::isEntityIdWithinLimit)
            .flatMap(entityId -> samlServices
                .stream()
                .filter(samlService -> samlService.matches(entityId))
                .filter(samlService -> {
                    LOGGER.trace("Resolving metadata for service [{}] via entity id [{}]", samlService.getName(), entityId);
                    return SamlRegisteredServiceMetadataAdaptor.get(resolver, samlService, entityId).isPresent();
                })
                .findFirst())
            .orElse(null);
    }

    private boolean isEntityIdWithinLimit(final String entityId) {
        if (maximumEntityIdLength > 0 && entityId.length() > maximumEntityIdLength) {
            LOGGER.warn("Entity id of length [{}] exceeds the maximum allowed length of [{}] and will not be matched",
                entityId.length(), maximumEntityIdLength);
            return false;
        }
        return true;
    }

    @Override
    public boolean supports(final RegisteredService registeredService, final Service service) {
        val matched = SamlRegisteredService.class.isAssignableFrom(registeredService.getClass())
            && registeredService.getFriendlyName().equalsIgnoreCase(SamlRegisteredService.FRIENDLY_NAME);
        return matched && SamlIdPServiceAttributeExtractor.extract(registeredService, service).isPresent();
    }

    @Override
    protected Pair<String, Class<? extends RegisteredService>> getRegisteredServiceIndexedType() {
        return Pair.of(SamlRegisteredService.FRIENDLY_NAME, SamlRegisteredService.class);
    }
}
