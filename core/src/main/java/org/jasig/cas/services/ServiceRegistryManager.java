/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.services;


/**
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public interface ServiceRegistryManager {
    /**
     * Method to manually add one service to the list.
     * 
     * @param service The service to add.
     */
    void addService(AuthenticatedService service);
    
    /**
     * Method to delete a service from the list.
     * 
     * @param serviceId The service to delete
     */
    boolean deleteService(String serviceId);
}
