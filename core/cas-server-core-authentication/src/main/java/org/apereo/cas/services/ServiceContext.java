package org.apereo.cas.services;

import org.apereo.cas.authentication.principal.Service;

/**
 * Simple container for holding a service principal and its corresponding registered service.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class ServiceContext {

    /** Service principal. */
    
    private final Service service;

    /** Registered service corresponding to service principal. */
    
    private final RegisteredService registeredService;

    /**
     * Creates a new instance with required parameters.
     *
     * @param service Service principal.
     * @param registeredService Registered service corresponding to given service.
     */
    public ServiceContext(final Service service, final RegisteredService registeredService) {
        this.service = service;
        this.registeredService = registeredService;
        if (!registeredService.matches(service)) {
            throw new IllegalArgumentException("Registered service does not match given service.");
        }
    }

    /**
     * Gets the service principal.
     *
     * @return Non-null service principal.
     */
    public Service getService() {
        return this.service;
    }

    /**
     * Gets the registered service for the service principal.
     *
     * @return Non-null registered service.
     */
    public RegisteredService getRegisteredService() {
        return this.registeredService;
    }
}
