/*
 * Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.Service;

/**
 * Interface for a Service Ticket. A service ticket is used to grant access to a
 * specific service.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public interface ServiceTicket extends Ticket {

    public static final String PREFIX = "ST";

    /**
     * Retrieve the service this ticket was given for.
     * 
     * @return the server.
     */
    Service getService();

    /**
     * Determine if this ticket was created at the same time as a
     * TicketGrantingTicket
     * 
     * @return true if it is, false otherwise.
     */
    boolean isFromNewLogin();

    /**
     * Method to allow you to set the fromNewLogin flag.
     * 
     * @param fromNewLogin
     */
    void setFromNewLogin(boolean fromNewLogin);

    /**
     * Method to grant a TicketGrantingTicket from this service to the
     * authentication. Analogous to the ProxyGrantingTicket.
     * 
     * @param authentication The Authentication we wish to grant a ticket for.
     * @return The ticket granting ticket.
     */
    TicketGrantingTicket grantTicketGrantingTicket(Authentication authentication);
}