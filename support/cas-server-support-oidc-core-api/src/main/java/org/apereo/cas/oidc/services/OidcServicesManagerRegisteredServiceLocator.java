package org.apereo.cas.oidc.services;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.DefaultServicesManagerRegisteredServiceLocator;
import org.apereo.cas.services.OidcRegisteredService;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.Ordered;

/**
 * This is {@link OidcServicesManagerRegisteredServiceLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
public class OidcServicesManagerRegisteredServiceLocator extends DefaultServicesManagerRegisteredServiceLocator {
    /**
     * Execution order of this locator.
     */
    static final int DEFAULT_ORDER = Ordered.HIGHEST_PRECEDENCE + 1;

    public OidcServicesManagerRegisteredServiceLocator() {
        setOrder(DEFAULT_ORDER);
        setRegisteredServiceFilter(
            (registeredService, service) -> {
                var match = supports(registeredService, service);
                if (match) {
                    val oidcService = (OidcRegisteredService) registeredService;
                    LOGGER.trace("Attempting to locate service [{}] via [{}]", service, oidcService);
                    match = CollectionUtils.firstElement(service.getAttributes().get(OAuth20Constants.CLIENT_ID))
                        .map(Object::toString)
                        .stream()
                        .anyMatch(clientId -> oidcService.getClientId().equalsIgnoreCase(clientId));
                }
                return match;
            });
    }

    @Override
    public boolean supports(final RegisteredService registeredService, final Service service) {
        return service.getAttributes().containsKey(OAuth20Constants.CLIENT_ID)
            && registeredService instanceof OidcRegisteredService;
    }
}

