/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket;

import org.jasig.cas.authentication.Service;
import org.jasig.cas.authentication.principal.Principal;

/**
 * Interface for a Service Ticket. A service ticket is used to grant access to a specific service.
 * 
 * @author Scott Battaglia
 * @version $Id$
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
     * Determine if this ticket was created at the same time as a TicketGrantingTicket 
     * @return true if it is, false otherwise.
     */
    boolean isFromNewLogin();
    
    /**
     * 
     * @return
     */
    TicketGrantingTicket grantTicketGrantingTicket(Principal principal);
}
