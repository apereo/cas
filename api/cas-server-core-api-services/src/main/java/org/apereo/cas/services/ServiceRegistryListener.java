package org.apereo.cas.services;

import org.springframework.core.Ordered;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link ServiceRegistryListener}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public interface ServiceRegistryListener extends Serializable, Ordered {
    /**
     * No op service registry listener.
     *
     * @return the service registry listener
     */
    static ServiceRegistryListener noOp() {
        return new ServiceRegistryListener() {
            @Serial
            private static final long serialVersionUID = -8064239596498367543L;
        };
    }

    /**
     * Pre save registered service.
     *
     * @param registeredService the registered service
     * @return the registered service
     */
    default RegisteredService preSave(final RegisteredService registeredService) {
        return registeredService;
    }

    /**
     * Post load registered service.
     *
     * @param registeredService the registered service
     * @return the registered service
     */
    default RegisteredService postLoad(final RegisteredService registeredService) {
        return registeredService;
    }

    @Override
    default int getOrder() {
        return 0;
    }
}
