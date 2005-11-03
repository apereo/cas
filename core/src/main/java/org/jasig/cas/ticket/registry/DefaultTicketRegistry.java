/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.registry;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.ticket.Ticket;

/**
 * Implementation of the TicketRegistry that is backed by a HashMap.
 * <p>
 * The underlying HashMap is not threadsafe. Each method is synchronized but
 * care should be taken that if multiple methods will be called, the code should
 * be placed in a synchronize block.
 * </p>
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class DefaultTicketRegistry implements TicketRegistry {

    /** The Commons Logging instance. */
    private final Log log = LogFactory.getLog(getClass());

    /** A HashMap to contain the tickets. */
    private final Map cache = new HashMap();

    /**
     * @throws IllegalArgumentException if the Ticket is null.
     */
    public synchronized void addTicket(final Ticket ticket) {
        if (ticket == null) {
            throw new IllegalArgumentException("ticket cannot be null");
        }

        if (log.isDebugEnabled()) {
            log.debug("Added ticket [" + ticket.getId() + "] to registry.");
        }
        this.cache.put(ticket.getId(), ticket);
    }

    /**
     * @throws IllegalArgumentException if class is null.
     * @throws ClassCastException if class does not match requested ticket
     * class.
     */
    public synchronized Ticket getTicket(final String ticketId,
        final Class clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("clazz cannot be null");
        }

        final Ticket ticket = this.getTicket(ticketId);

        if (ticket == null) {
            return null;
        }

        if (!clazz.isAssignableFrom(ticket.getClass())) {
            throw new ClassCastException("Ticket [" + ticket.getId()
                + " is of type " + ticket.getClass()
                + " when we were expecting " + clazz);
        }

        return ticket;
    }

    public synchronized Ticket getTicket(final String ticketId) {
        if (log.isDebugEnabled()) {
            log.debug("Attempting to retrieve ticket [" + ticketId + "]");
        }
        final Ticket ticket = (Ticket) this.cache.get(ticketId);

        if (ticket != null) {
            log.debug("Ticket [" + ticketId + "] found in registry.");
        }

        return ticket;
    }

    public synchronized boolean deleteTicket(final String ticketId) {
        if (log.isDebugEnabled()) {
            log.debug("Removing ticket [" + ticketId + "] from registry");
        }

        return (this.cache.remove(ticketId) != null);
    }

    public synchronized Collection getTickets() {
        return Collections.unmodifiableCollection(this.cache.values());
    }
}
