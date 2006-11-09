/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services.service;

import org.jasig.cas.services.domain.RegisteredService;

/**
 * Interface for managing services that utilize CAS.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public interface ServiceManager {

    /**
     * Add a new service.
     * 
     * @param registeredService the service to add.
     * @return true if it was successfully added, false otherwise.
     */
    boolean addService(RegisteredService registeredService);

    /**
     * Update a particular service.
     * 
     * @param registeredService the service to update.
     * @return true if it was updated, false otherwise.
     */
    boolean updateService(RegisteredService registeredService);

    /**
     * Delete a service based on its id.
     * 
     * @param id the id of the service to delete.
     * @return true if it was deleted, false otherwise.
     */
    boolean deleteService(String id);

    /**
     * Retrieve all of the services.
     * 
     * @return the array of services (or an array of size 0).
     */
    RegisteredService[] getAllServices();

    /**
     * Retrieve a service based off the id.
     * 
     * @param id the id to search for.
     * @return the service matched by tthe id, null otherwise.
     */
    RegisteredService getServiceById(String id);

    /**
     * Retrieve a service based off of is url.
     * 
     * @param url the url to search for.
     * @return the service matched by the url, null otherwise.
     */
    RegisteredService getServiceByUrl(String url);
}
