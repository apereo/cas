package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Service;

import org.springframework.core.Ordered;

import java.util.Collection;
import java.util.function.Predicate;

/**
 * This is {@link ServicesManagerRegisteredServiceLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@FunctionalInterface
public interface ServicesManagerRegisteredServiceLocator extends Ordered {
    /**
     * Locate registered service.
     *
     * @param candidates      the candidates
     * @param service         the service id
     * @param predicateFilter the predicate filter
     * @return the registered service
     */
    RegisteredService locate(Collection<RegisteredService> candidates, Service service,
        Predicate<RegisteredService> predicateFilter);

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
