/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.services;

import java.util.List;

import org.jasig.cas.authentication.principal.Service;

/**
 * Interface for a registry that holds services. ServiceRegistry is only
 * concerned with the retrieval of a service and the determination of if the
 * service exists. The ServiceRegistryManager is concerned with the maintanance
 * of the list of services.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public interface ServiceRegistry {

    /**
     * Retrieve a RegisteredService, if it exists. This should guarantee only
     * one service per id exists.
     * 
     * @param serviceId the service to look for.
     * @return the service if it exists, false otherwise.
     */
    RegisteredService findServiceBy(Service service);
    
    /**
     * Returns all the complete list of services.
     * 
     * @return the complete list of services.
     */
    List<RegisteredService> getAllServices();

    /**
     * Helper to determine if a service matches a service in the registry.
     * 
     * @param service the service to check
     * @return true if it does, false otherwise.
     */
    boolean matchesExistingService(Service service);
    
    void setEnabled(boolean enabled);
    
    boolean isEnabled();
}
