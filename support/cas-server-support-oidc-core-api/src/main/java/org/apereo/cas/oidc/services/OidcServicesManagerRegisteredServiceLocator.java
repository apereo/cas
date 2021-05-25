package org.apereo.cas.oidc.services;

import org.apereo.cas.services.DefaultServicesManagerRegisteredServiceLocator;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.util.CollectionUtils;
import org.springframework.core.Ordered;

/**
 * This is {@link OidcServicesManagerRegisteredServiceLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public class OidcServicesManagerRegisteredServiceLocator extends DefaultServicesManagerRegisteredServiceLocator {
    public OidcServicesManagerRegisteredServiceLocator() {
        setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        setRegisteredServiceFilter((registeredService, service) -> service.getAttributes().containsKey(OAuth20Constants.CLIENT_ID)
                && OidcRegisteredService.class.isAssignableFrom(registeredService.getClass())
                && CollectionUtils.firstElement(service.getAttributes().get(OAuth20Constants.CLIENT_ID))
                .map(id -> ((OidcRegisteredService) registeredService).getClientId().equals(id))
                .orElse(false)
        );
    }
}

