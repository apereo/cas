/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.registry.support;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.registry.RegistryCleaner;
import org.jasig.cas.ticket.registry.TicketRegistry;

/**
 * Class to look for expired tickets and remove them from the registry.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public class DefaultTicketRegistryCleaner implements RegistryCleaner {

    protected final Log log = LogFactory.getLog(getClass());

    private TicketRegistry ticketRegistry;

    public void clean() {
        final List ticketsToRemove = new ArrayList();
        log
            .info("Starting cleaning of expired tickets from ticket registry at ["
                + new Date() + "]");
        synchronized (this.ticketRegistry) {
            for (Iterator iter = this.ticketRegistry.getTickets().iterator(); iter
                .hasNext();) {
                final Ticket ticket = (Ticket)iter.next();

                if (ticket.isExpired())
                    ticketsToRemove.add(ticket);
            }

            for (Iterator iter = ticketsToRemove.iterator(); iter.hasNext();) {
                final Ticket ticket = (Ticket)iter.next();
                this.ticketRegistry.deleteTicket(ticket.getId());
            }
        }
        log
            .info("Finished cleaning of expired tickets from ticket registry at ["
                + new Date() + "]");
    }

    /**
     * @param ticketRegistry The ticketRegistry to set.
     */
    public void setTicketRegistry(final TicketRegistry ticketRegistry) {
        this.ticketRegistry = ticketRegistry;
    }
}