package org.apereo.cas.ws.idp.services;

import org.apereo.cas.services.DefaultServicesManagerRegisteredServiceLocator;

import org.springframework.core.Ordered;

/**
 * This is {@link WsFederationServicesManagerRegisteredServiceLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public class WsFederationServicesManagerRegisteredServiceLocator extends DefaultServicesManagerRegisteredServiceLocator {
    public WsFederationServicesManagerRegisteredServiceLocator() {
        setOrder(Ordered.HIGHEST_PRECEDENCE);
        setRegisteredServiceFilter((registeredService, serviceId) -> registeredService.getClass().equals(WSFederationRegisteredService.class));
    }
}

