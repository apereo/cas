package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Service;

import org.springframework.core.Ordered;

import java.util.Collection;

/**
 * This is {@link ServicesManagerRegisteredServiceLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public interface ServicesManagerRegisteredServiceLocator extends Ordered {
    /**
     * Locate registered service.
     *
     * @param candidates the candidates
     * @param service    the service id
     * @return the registered service
     */
    RegisteredService locate(Collection<RegisteredService> candidates, Service service);

    /**
     * Can this locator find/locate the given registered service
     * based on the provided service request?
     *
     * @param registeredService the registered service
     * @param service           the service
     * @return true/false
     */
    boolean supports(RegisteredService registeredService, Service service);

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    default String getName() {
        return getClass().getSimpleName();
    }
}
