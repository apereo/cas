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
     * Return value indicates whether the serviceid mapped to a Service which
     *  was registered and is now as a result of this method invocation unregistered
     *  (true) or the serviceId did not map to a registered service (false).
     * 
     * @param serviceId The service to delete
     * @return true if method invocation resulted in a change, false otherwise
     */
    boolean deleteService(String serviceId);
}
