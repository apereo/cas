package org.apereo.cas.support.saml.services;

import org.apereo.cas.services.DefaultServicesManagerRegisteredServiceLocator;
import org.apereo.cas.support.saml.services.idp.metadata.SamlRegisteredServiceServiceProviderMetadataFacade;
import org.apereo.cas.support.saml.services.idp.metadata.cache.SamlRegisteredServiceCachingMetadataResolver;

import lombok.val;
import org.springframework.core.Ordered;

/**
 * This is {@link SamlIdPServicesManagerRegisteredServiceLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public class SamlIdPServicesManagerRegisteredServiceLocator extends DefaultServicesManagerRegisteredServiceLocator {
    public SamlIdPServicesManagerRegisteredServiceLocator(final SamlRegisteredServiceCachingMetadataResolver resolver) {
        setOrder(Ordered.HIGHEST_PRECEDENCE);
        setRegisteredServiceFilter((registeredService, serviceId) -> {
            if (registeredService.getClass().equals(SamlRegisteredService.class)) {
                val samlService = SamlRegisteredService.class.cast(registeredService);
                val adaptor = SamlRegisteredServiceServiceProviderMetadataFacade.get(resolver, samlService, serviceId);
                return adaptor.isPresent();
            }
            return false;
        });
    }
}
