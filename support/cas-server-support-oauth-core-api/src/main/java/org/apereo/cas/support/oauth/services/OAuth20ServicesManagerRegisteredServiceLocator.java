package org.apereo.cas.support.oauth.services;

import org.apereo.cas.services.DefaultServicesManagerRegisteredServiceLocator;
import org.apereo.cas.support.oauth.OAuth20Constants;

import org.springframework.core.Ordered;

import java.util.Objects;

/**
 * This is {@link OAuth20ServicesManagerRegisteredServiceLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public class OAuth20ServicesManagerRegisteredServiceLocator extends DefaultServicesManagerRegisteredServiceLocator {
    public OAuth20ServicesManagerRegisteredServiceLocator() {
        setOrder(Ordered.HIGHEST_PRECEDENCE);
        setRegisteredServiceFilter((registeredService, service) -> registeredService instanceof OAuthRegisteredService
                && service.getAttributes().containsKey(OAuth20Constants.CLIENT_ID)
                && service.getAttributes().get(OAuth20Constants.CLIENT_ID)
                .stream()
                .filter(Objects::nonNull)
                .map(String.class::cast)
                .anyMatch(clientId -> clientId.equals(((OAuthRegisteredService) registeredService).getClientId())));
    }
}

