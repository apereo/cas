package org.apereo.cas.oidc.services;

import org.apereo.cas.services.DefaultServicesManagerRegisteredServiceLocator;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.OAuth20Constants;

import org.springframework.core.Ordered;

import java.util.Objects;

/**
 * This is {@link OidcServicesManagerRegisteredServiceLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public class OidcServicesManagerRegisteredServiceLocator extends DefaultServicesManagerRegisteredServiceLocator {
    public OidcServicesManagerRegisteredServiceLocator() {
        setOrder(Ordered.HIGHEST_PRECEDENCE);
        setRegisteredServiceFilter(
            (registeredService, service) -> OidcRegisteredService.class.isAssignableFrom(registeredService.getClass())
                && service.getAttributes().containsKey(OAuth20Constants.CLIENT_ID)
                && service.getAttributes().get(OAuth20Constants.CLIENT_ID)
                    .stream()
                    .filter(Objects::nonNull)
                    .map(String.class::cast)
                    .anyMatch(clientId -> clientId.equals(((OidcRegisteredService) registeredService).getClientId())));
    }
}

