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
import org.springframework.core.Ordered;

/**
 * This is {@link SamlIdPServicesManagerRegisteredServiceLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
public class SamlIdPServicesManagerRegisteredServiceLocator extends BaseServicesManagerRegisteredServiceLocator {

    public SamlIdPServicesManagerRegisteredServiceLocator(
        final SamlRegisteredServiceCachingMetadataResolver resolver) {

        setOrder(Ordered.HIGHEST_PRECEDENCE);
        setRegisteredServiceFilter((registeredService, service) -> {
            val parameterValue = SamlIdPServiceAttributeExtractor.extract(registeredService, service);
            return parameterValue
                .map(pair -> {
                    val attribute = pair.getLeft();
                    val attributeValue = pair.getRight();
                    LOGGER.trace("Located service attribute [{}] with value [{}]", attribute, attributeValue);
                    return attribute.getEntityIdFrom(resolver, attributeValue);
                })
                .filter(StringUtils::isNotBlank)
                .filter(StringUtils::isNotBlank)
                .filter(registeredService::matches)
                .stream()
                .anyMatch(entityId -> {
                    LOGGER.trace("Resolving metadata for service [{}] via entity id [{}]", registeredService.getName(), entityId);
                    val samlService = (SamlRegisteredService) registeredService;
                    val adaptor = SamlRegisteredServiceMetadataAdaptor.get(resolver, samlService, entityId);
                    return adaptor.isPresent();
                });
        });
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
