/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket;

import org.jasig.cas.Service;


/**
 * Interface for TicketGrantingTickets within the CentralAuthenticationService domain.
 * 
 * This interface exposes methods for internal use that the CentralAuthenticationService
 * may use to update the state of the ticket.
 * 
 * @author Scott Battaglia
 * @version $Id$
 *
 */
public interface InternalTicketGrantingTicket extends TicketGrantingTicket {
	
    /**
     * Grant a ServiceTicket for a specific service
     * 
     * @param service The service for which we are granting a ticket
     * @return
     */
    ServiceTicket grantServiceTicket(Service service);
}
