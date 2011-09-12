/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.ticket;

import java.util.List;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;

/**
 * Interface for a ticket granting ticket. A TicketGrantingTicket is the main
 * access into the CAS service layer. Without a TicketGrantingTicket, a user of
 * CAS cannot do anything.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public interface TicketGrantingTicket extends Ticket {

    /** The prefix to use when generating an id for a TicketGrantingTicket. */
    String PREFIX = "TGT";

    /**
     * Method to retrieve the authentication.
     * 
     * @return the authentication
     */
    Authentication getAuthentication();

    /**
     * Grant a ServiceTicket for a specific service.
     * 
     * @param id The unique identifier for this ticket.
     * @param service The service for which we are granting a ticket
     * @return the service ticket granted to a specific service for the
     * principal of the TicketGrantingTicket
     */
    ServiceTicket grantServiceTicket(String id, Service service,
        ExpirationPolicy expirationPolicy, boolean credentialsProvided);

    /**
     * Explicitly expire a ticket.  This method will log out of any service associated with the
     * Ticket Granting Ticket.
     * 
     */
    void expire();

    /**
     * Convenience method to determine if the TicketGrantingTicket is the root
     * of the hierarchy of tickets.
     * 
     * @return true if it has no parent, false otherwise.
     */
    boolean isRoot();

    /**
     * Method to retrieve the chained list of Authentications for this
     * TicketGrantingTicket.
     * 
     * @return the list of principals
     */
    List<Authentication> getChainedAuthentications();
}
