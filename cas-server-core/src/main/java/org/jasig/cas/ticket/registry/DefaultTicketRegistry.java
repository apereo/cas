/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.ticket.registry;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jasig.cas.ticket.Ticket;
import org.springframework.util.Assert;

/**
 * Implementation of the TicketRegistry that is backed by a ConcurrentHashMap.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class DefaultTicketRegistry extends AbstractTicketRegistry {

    /** A HashMap to contain the tickets. */
    // TODO optimize this
    private final Map<String, Ticket> cache = new ConcurrentHashMap<String, Ticket>();

    /**
     * @throws IllegalArgumentException if the Ticket is null.
     */
    public void addTicket(final Ticket ticket) {
        Assert.notNull(ticket, "ticket cannot be null");

        if (log.isDebugEnabled()) {
            log.debug("Added ticket [" + ticket.getId() + "] to registry.");
        }
        this.cache.put(ticket.getId(), ticket);
    }

    public Ticket getTicket(final String ticketId) {
        if (log.isDebugEnabled()) {
            log.debug("Attempting to retrieve ticket [" + ticketId + "]");
        }
        final Ticket ticket = this.cache.get(ticketId);

        if (ticket != null) {
            log.debug("Ticket [" + ticketId + "] found in registry.");
        }

        return ticket;
    }

    public boolean deleteTicket(final String ticketId) {
        if (log.isDebugEnabled()) {
            log.debug("Removing ticket [" + ticketId + "] from registry");
        }

        return (this.cache.remove(ticketId) != null);
    }

    public Collection<Ticket> getTickets() {
        return Collections.unmodifiableCollection(this.cache.values());
    }
}
