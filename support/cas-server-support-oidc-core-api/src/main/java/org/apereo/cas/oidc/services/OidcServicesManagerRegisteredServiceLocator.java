package org.apereo.cas.oidc.services;

import org.apereo.cas.services.DefaultServicesManagerRegisteredServiceLocator;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.OAuth20Constants;

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
        setRegisteredServiceFilter(
            (registeredService, service) -> service.getAttributes().containsKey(OAuth20Constants.CLIENT_ID)
                && registeredService instanceof OidcRegisteredService);
    }
}

