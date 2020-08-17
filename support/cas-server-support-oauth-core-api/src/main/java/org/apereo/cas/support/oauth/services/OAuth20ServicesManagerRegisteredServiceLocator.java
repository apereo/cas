package org.apereo.cas.support.oauth.services;

import org.apereo.cas.services.DefaultServicesManagerRegisteredServiceLocator;

import org.springframework.core.Ordered;

/**
 * This is {@link OAuth20ServicesManagerRegisteredServiceLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public class OAuth20ServicesManagerRegisteredServiceLocator extends DefaultServicesManagerRegisteredServiceLocator {
    public OAuth20ServicesManagerRegisteredServiceLocator() {
        setOrder(Ordered.HIGHEST_PRECEDENCE);
        setRegisteredServiceFilter((registeredService, serviceId) -> registeredService.getClass().equals(OAuthRegisteredService.class));
    }
}

