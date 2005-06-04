/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

import java.util.Collection;

/**
 * Interface for a registry that holds services. ServiceRegistry is only
 * concerned with the retrieval of a service and the determination of if the
 * service exists. The ServiceRegistryManager is concerned with the maintanance
 * of the list of services.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public interface ServiceRegistry {

    /**
     * Method to determine if the service exists in the registry.
     * 
     * @param serviceId the service to check
     * @return true if the service exists. False otherwise.
     */
    boolean serviceExists(String serviceId);

    /**
     * Retrieve a service from the registry matched to the provided serviceId.
     * 
     * @param serviceId the id of the service to retrieve.
     * @return the service if found, null otherwise.
     */
    RegisteredService getService(String serviceId);

    /**
     * Method to return the list of services in the registry.
     * 
     * @return A list of services
     */
    Collection getServices();
}
