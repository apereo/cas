/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket;

import org.jasig.cas.authentication.principal.Principal;

/**
 * Interface for a ticket granting ticket.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public interface TicketGrantingTicket extends Ticket {
    
    /**
     * Method to retrieve the username.
     * 
     * @return the username
     */
    Principal getPrincipal();
}
