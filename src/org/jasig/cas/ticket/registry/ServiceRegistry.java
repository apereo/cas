/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */

package org.jasig.cas.ticket.registry;

import java.util.List;

/**
 * Registry that contains a list of valid services that service tickets
 * must use for validation.  If a service ticket does not exist in the 
 * registry (other than the registry being empty), a service ticket should
 * not be granted.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public interface ServiceRegistry {

	/**
	 * Method to manually add one service to the list.
	 * 
	 * @param service The service to add.
	 */
    public void addService(String service);

    /**
     * Method to determine if the service exists in the registry.
     * Default behavior of returning true if there are no services registered in the system.
     * @param service the service to check
     * @return true if the service exists or there are no services registered.  False otherwise.
     */
    public boolean serviceExists(String service);

    /**
     * Method to delete a service from the list.
     * @param service The service to delete
     */
    public void deleteService(String service);

    /**
     * Method to return the list of services  in the registry.
     * @return A list of services
     */
    public List getServices();
}