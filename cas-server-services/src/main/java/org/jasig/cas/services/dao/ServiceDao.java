/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services.dao;

import java.util.List;

import org.jasig.cas.services.domain.RegisteredService;

/**
 * DAO layer for retrieval of Services from the data store.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 */
public interface ServiceDao {

    /**
     * Find a service by its unique identifier.
     * 
     * @param id the id of the service.
     * @return the Registered Service.
     */
    RegisteredService findServiceById(String id);

    /**
     * Find a service by its unique url.
     * 
     * @param id the id of the service.
     * @return the Registered Service.
     */
    RegisteredService findServiceByUrl(String url);

    /**
     * Retrieve the list of all services.
     * 
     * @return all services stored in the data store.
     */
    List getAllServices();

    /**
     * Delete the service referenced by this id.
     * 
     * @param id the id of the service to delete.
     * @return true if it was deleted, false otherwise.
     */
    boolean deleteById(String id);

    /**
     * Save the service to the data store (either adding or updating).
     * 
     * @param registeredService the service to save.
     * @return true if it was saved, false otherwise.
     */
    boolean save(RegisteredService registeredService);
}
