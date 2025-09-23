package org.apereo.cas.ws.idp.services;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.BaseServicesManagerRegisteredServiceLocator;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.ws.idp.WSFederationConstants;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.Ordered;

/**
 * This is {@link WsFederationServicesManagerRegisteredServiceLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Slf4j
public class WsFederationServicesManagerRegisteredServiceLocator extends BaseServicesManagerRegisteredServiceLocator {
    public WsFederationServicesManagerRegisteredServiceLocator() {
        setOrder(Ordered.HIGHEST_PRECEDENCE);
        setRegisteredServiceFilter(
            (registeredService, service) -> {
                var match = supports(registeredService, service);
                if (match) {
                    val wsfedService = (WSFederationRegisteredService) registeredService;
                    LOGGER.trace("Attempting to locate service [{}] via [{}]", service, wsfedService);
                    match = CollectionUtils.firstElement(service.getAttributes().get(WSFederationConstants.WREPLY))
                        .map(Object::toString)
                        .stream()
                        .anyMatch(wsfedService::matches);
                }
                return match;
            });
    }

    @Override
    protected Pair<String, Class<? extends RegisteredService>> getRegisteredServiceIndexedType() {
        return Pair.of(WSFederationRegisteredService.FRIENDLY_NAME, WSFederationRegisteredService.class);
    }

    @Override
    public boolean supports(final RegisteredService registeredService, final Service service) {
        return service.getAttributes().containsKey(WSFederationConstants.WREPLY)
            && registeredService instanceof WSFederationRegisteredService;
    }
}

