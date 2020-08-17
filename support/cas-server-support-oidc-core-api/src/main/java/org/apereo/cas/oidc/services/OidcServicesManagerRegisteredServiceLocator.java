package org.apereo.cas.oidc.services;

import org.apereo.cas.services.DefaultServicesManagerRegisteredServiceLocator;
import org.apereo.cas.services.OidcRegisteredService;

import org.springframework.core.Ordered;

/**
 * This is {@link OidcServicesManagerRegisteredServiceLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public class OidcServicesManagerRegisteredServiceLocator extends DefaultServicesManagerRegisteredServiceLocator {
    public OidcServicesManagerRegisteredServiceLocator() {
        setOrder(Ordered.HIGHEST_PRECEDENCE);
        setRegisteredServiceFilter((registeredService, serviceId) -> registeredService.getClass().equals(OidcRegisteredService.class));
    }
}

