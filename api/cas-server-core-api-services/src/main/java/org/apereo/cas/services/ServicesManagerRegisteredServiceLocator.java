package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.query.RegisteredServiceQueryIndex;
import org.apereo.cas.util.NamedObject;

import org.springframework.core.Ordered;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This is {@link ServicesManagerRegisteredServiceLocator}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public interface ServicesManagerRegisteredServiceLocator extends Ordered, NamedObject {
    /**
     * Default order, used to determine the execution sequence.
     */
    int DEFAULT_ORDER = -1000;

    /**
     * Locate registered service.
     *
     * @param candidates the candidates
     * @param service    the service id
     * @return the registered service
     */
    RegisteredService locate(Collection<? extends RegisteredService> candidates, Service service);

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
        return DEFAULT_ORDER;
    }

    default List<RegisteredServiceQueryIndex> getRegisteredServiceIndexes() {
        return new ArrayList<>();
    }
}
