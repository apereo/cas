/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.factory;

import org.jasig.cas.authentication.principal.Principal;
import org.jasig.cas.ticket.CasAttributes;
import org.jasig.cas.ticket.Ticket;

/**
 * @author Scott Battaglia
 * @version $Id$
 */
public interface TicketFactory {

    /**
     * Method to create a new ticket.
     * 
     * @param clazz The class of the ticket we want to create.
     * @param request The parameters to use to create the ticket.
     * @param parentTicket A possible parent ticket.
     * @return the new ticket.
     */
    Ticket getTicket(Class clazz, Principal principal, CasAttributes request, Ticket parentTicket);
}
