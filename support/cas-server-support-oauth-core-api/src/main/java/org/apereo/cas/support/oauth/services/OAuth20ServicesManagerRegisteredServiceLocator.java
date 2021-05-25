package org.apereo.cas.support.oauth.services;

import org.apereo.cas.services.DefaultServicesManagerRegisteredServiceLocator;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.util.CollectionUtils;
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
        setRegisteredServiceFilter((registeredService, service) -> service.getAttributes().containsKey(OAuth20Constants.CLIENT_ID)
            && OAuthRegisteredService.class.isAssignableFrom(registeredService.getClass())
            && CollectionUtils.firstElement(service.getAttributes().get(OAuth20Constants.CLIENT_ID))
                .map(id -> ((OAuthRegisteredService) registeredService).getClientId().equals(id))
                .orElse(false)
        );
    }
}

