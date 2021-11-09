package org.apereo.cas.support.oauth.services;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.DefaultServicesManagerRegisteredServiceLocator;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.Ordered;

/**
 * This is {@link OAuth20ServicesManagerRegisteredServiceLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
public class OAuth20ServicesManagerRegisteredServiceLocator extends DefaultServicesManagerRegisteredServiceLocator {
    public OAuth20ServicesManagerRegisteredServiceLocator() {
        setOrder(Ordered.HIGHEST_PRECEDENCE);
        setRegisteredServiceFilter((registeredService, service) -> {
            var match = supports(registeredService, service);
            if (match) {
                val oauthService = (OAuthRegisteredService) registeredService;
                LOGGER.trace("Attempting to locate service [{}] via [{}]", service, oauthService);
                match = CollectionUtils.firstElement(service.getAttributes().get(OAuth20Constants.CLIENT_ID))
                    .map(Object::toString)
                    .stream()
                    .anyMatch(clientId -> oauthService.getClientId().equalsIgnoreCase(clientId));
            }
            return match;
        });
    }

    @Override
    public boolean supports(final RegisteredService registeredService, final Service service) {
        return service.getAttributes().containsKey(OAuth20Constants.CLIENT_ID)
            && registeredService instanceof OAuthRegisteredService;
    }
}

