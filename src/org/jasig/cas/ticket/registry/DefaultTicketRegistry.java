/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.registry;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.ticket.InvalidTicketException;
import org.jasig.cas.ticket.Ticket;

/**
 * Generic registry that holds all tickets of any kind in a hash map.
 * 
 * @author Scott Battaglia
 * @version $Id$
 */
public class DefaultTicketRegistry implements TicketRegistry {

    protected final Log log = LogFactory.getLog(getClass());

    final private Map cache = new HashMap();

    /**
     * @see org.jasig.cas.ticket.registry.TicketRegistry#addTicket(org.jasig.cas.ticket.Ticket)
     */
    public void addTicket(final Ticket ticket) {
        log.debug("Added ticket [" + ticket.getId() + "] to registry.");
        this.cache.put(ticket.getId(), ticket);
    }

    /**
     * @see org.jasig.cas.ticket.registry.TicketRegistry#getTicket(java.lang.String, java.lang.Class)
     */
    public Ticket getTicket(final String ticketId, final Class clazz) {
    	final Ticket ticket = this.getTicket(ticketId);

        if (ticket == null)
            return null;

        if (!clazz.isAssignableFrom(ticket.getClass()))
            throw new InvalidTicketException("Ticket [" + ticket.getId() + " is of type "
                + ticket.getClass() + " when we were expecting " + clazz);

        return ticket;
    }

	/**
	 * @see org.jasig.cas.ticket.registry.TicketRegistry#getTicket(java.lang.String)
	 */
	public Ticket getTicket(String ticketId) {
        log.debug("Attempting to retrieve ticket [" + ticketId + "]");
        final Ticket ticket = (Ticket)this.cache.get(ticketId);

        if (ticket != null)
        	log.debug("Ticket [" + ticketId + "] found in registry.");

        return ticket;
	}
    /**
     * @see org.jasig.cas.ticket.registry.TicketRegistry#deleteTicket(java.lang.String)
     */
    public boolean deleteTicket(final String ticketId) {
        log.debug("Removing ticket [" + ticketId + "] from registry");
        return this.cache.remove(ticketId) == null ? false : true;
    }

    /**
     * @see org.jasig.cas.ticket.registry.TicketRegistry#getTickets()
     */
    public Collection getTickets() {
        return Collections.unmodifiableCollection(this.cache.values());
    }
}
