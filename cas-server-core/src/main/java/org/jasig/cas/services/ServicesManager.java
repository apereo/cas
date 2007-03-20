/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import java.util.Collection;

import org.jasig.cas.authentication.principal.Service;

/**
 * Manages the storage, retrieval, and matching of Services wishing to use CAS
 * and services that have been registered with CAS.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public interface ServicesManager {

    /**
     * Register a service with CAS, or update an existing an entry.
     * 
     * @param registeredService the RegisteredService to update or add.
     */
    void save(RegisteredService registeredService);

    /**
     * Delete the entry for this RegisteredService.
     * 
     * @param registeredService the registeredService to delete.
     * @return true if it was deleted, false otherwise.
     */
    boolean delete(RegisteredService registeredService);

    /**
     * Find a RegisteredService by matching with the supplied service.
     * 
     * @param service the service to match with.
     * @return the RegisteredService that matches the supplied service.
     */
    RegisteredService findServiceBy(Service service);

    /**
     * Retrieve a Service based on its identifier.
     * 
     * @param id the id of the service.
     * @return the RegisteredService that matches the id.
     */
    RegisteredService findServiceBy(long id);

    /**
     * Retrieve the collection of all registered services.
     * 
     * @return the collection of all services.
     */
    Collection<RegisteredService> getAllServices();

    /**
     * Convenience method to let one know if a service exists in the data store.
     * 
     * @param service the service to check.
     * @return true if it exists, false otherwise.
     */
    boolean matchesExistingService(Service service);
}
