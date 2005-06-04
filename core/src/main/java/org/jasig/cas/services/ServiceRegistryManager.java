/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

/**
 * Interface to a class that can manage the registry for services. The Registry
 * manager has the ability to add or delete a service as well as clear out the
 * whole registry.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public interface ServiceRegistryManager {

    /**
     * Method to manually add one service to the list.
     * 
     * @param service The service to add.
     */
    void addService(RegisteredService service);

    /**
     * Method to delete a service from the list. Return value indicates whether
     * the serviceid mapped to a Service which was registered and is now as a
     * result of this method invocation unregistered (true) or the serviceId did
     * not map to a registered service (false).
     * 
     * @param serviceId The service to delete.
     * @return true if method invocation resulted in a change, false otherwise.
     */
    boolean deleteService(String serviceId);

    /**
     * Method to clear the registry of all entries.
     */
    void clear();
}
