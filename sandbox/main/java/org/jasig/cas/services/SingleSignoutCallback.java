/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.services;

/**
 * Strategy to implement a callback to a service for the purposes of logging
 * out.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public interface SingleSignoutCallback {

    /**
     * Method to initiate a sign out request.
     * 
     * @param authenticatedService the service we are signing out of
     * @param serviceTicketId The service ticket.
     * @return true if we signed out, false otherwise.
     */
    boolean signOut(CallbackRegisteredService authenticatedService,
        String serviceTicketId);
}
