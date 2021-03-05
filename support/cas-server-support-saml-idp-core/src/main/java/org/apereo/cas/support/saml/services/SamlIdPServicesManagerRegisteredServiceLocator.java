package org.apereo.cas.support.saml.services;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.DefaultServicesManagerRegisteredServiceLocator;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.saml.SamlIdPConstants;
import org.apereo.cas.support.saml.SamlProtocolConstants;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.Ordered;

import java.util.Set;

/**
 * This is {@link SamlIdPServicesManagerRegisteredServiceLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
public class SamlIdPServicesManagerRegisteredServiceLocator extends DefaultServicesManagerRegisteredServiceLocator {
    private static final Set<String> SAML_PROTOCOL_PARAMETERS = Set.of(
        SamlProtocolConstants.PARAMETER_SAML_REQUEST,
        SamlProtocolConstants.PARAMETER_ENTITY_ID,
        SamlIdPConstants.PROVIDER_ID);

    public SamlIdPServicesManagerRegisteredServiceLocator(final SamlRegisteredServiceCachingMetadataResolver resolver) {
        setOrder(Ordered.HIGHEST_PRECEDENCE);
        setRegisteredServiceFilter((registeredService, service) -> {
            val isSamlServiceProvider = isSamlRegisteredService(registeredService, service);
            if (isSamlServiceProvider && registeredService.matches(service.getId())) {
                val samlService = SamlRegisteredService.class.cast(registeredService);
                val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(resolver, samlService, service.getId());
                return adaptor.isPresent();
            }
            return false;
        });
    }

    /**
     * Is saml registered service?
     *
     * @param registeredService the registered service
     * @param service           the service
     * @return the boolean
     */
    protected boolean isSamlRegisteredService(final RegisteredService registeredService, final Service service) {
        if (registeredService instanceof SamlRegisteredService) {
            val attributes = service.getAttributes();
            LOGGER.trace("Reviewing service attributes [{}] for service [{}]", attributes, service.getId());

            return SAML_PROTOCOL_PARAMETERS
                .stream()
                .anyMatch(attributes::containsKey);
        }
        return false;
    }
}
