/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket;

import java.util.List;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.Service;

/**
 * Interface for a ticket granting ticket.
 * 
 * @author Scott Battaglia
 * @version $Id: TicketGrantingTicket.java,v 1.1 2005/02/15 05:06:38 sbattaglia
 * Exp $
 */
public interface TicketGrantingTicket extends Ticket {

    public static final String PREFIX = "TGT";

    /**
     * Method to retrieve the authentication.
     * 
     * @return the authentication
     */
    Authentication getAuthentication();

    /**
     * Grant a ServiceTicket for a specific service
     * 
     * @param service The service for which we are granting a ticket
     * @return the service ticket granted to a specific service for the
     * principal of the TicketGrantingTicket
     */
    ServiceTicket grantServiceTicket(Service service);

    /**
     * Explicitly expire a ticket.
     */
    void expire();

    /**
     * Convenience method to determine if the TicketGrantingTicket is the root
     * of the heirachy of tickets.
     * 
     * @return true if it has no parent, false otherwise.
     */
    boolean isRoot();

    /**
     * Method to retrieve the chained list of principals for this
     * TicketGrantingTicket
     * 
     * @return the list of principals
     */
    List getChainedPrincipals();
}