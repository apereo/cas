/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.ticket.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.util.annotation.NotNull;
import org.jboss.cache.CacheException;
import org.jboss.cache.Node;
import org.jboss.cache.TreeCache;

/**
 * Implementation of TicketRegistry that is backed by a JBoss TreeCache.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 *
 */
public final class JBossCacheTicketRegistry extends AbstractDistributedTicketRegistry {
    
    /** Indicator of what tree branch to put tickets in. */
    private static final String FQN_TICKET = "ticket";

    /** Instance of JBoss TreeCache. */
    @NotNull
    private TreeCache cache;
    
    
    
    protected void updateTicket(Ticket ticket) {
        try {
            this.cache.put(FQN_TICKET, ticket.getId(), ticket);
        } catch (final CacheException e) {
            throw new RuntimeException(e);
        }
    }

    public void addTicket(final Ticket ticket) {
        try {
            if (log.isDebugEnabled()){
                log.debug("Adding ticket to registry for: " + ticket.getId());
            }
            this.cache.put(FQN_TICKET, ticket.getId(), ticket);
        } catch (final CacheException e) {
            log.error(e, e);
            throw new RuntimeException(e);
        }
    }

    public boolean deleteTicket(final String ticketId) {
        try {
            if (log.isDebugEnabled()){
                log.debug("Removing ticket from registry for: " + ticketId);
            }
            return this.cache.remove(FQN_TICKET, ticketId) != null;
        } catch (final CacheException e) {
            log.error(e, e);
            return false;
        }
    }

    /**
     * Returns a proxied instance.
     * 
     * @see org.jasig.cas.ticket.registry.TicketRegistry#getTicket(java.lang.String)
     */
    public Ticket getTicket(final String ticketId) {
        try {
            if (log.isDebugEnabled()){
                log.debug("Retrieving ticket from registry for: " + ticketId);
            }
            return getProxiedTicketInstance((Ticket) this.cache.get(FQN_TICKET, ticketId));
        } catch (final CacheException e) {
            log.error(e, e);
            return null;
        }
    }

    public Collection<Ticket> getTickets() {
        try {
            final Node node = this.cache.get(FQN_TICKET);

            if (node == null) {
                return Collections.emptyList();
            }
            
            final Set<Object> keys = node.getDataKeys();
            final List<Ticket> list = new ArrayList<Ticket>();

            for (final Object key : keys) {
                list.add((Ticket) node.get(key));
            }

            return list;
        } catch (final CacheException e) {
            return Collections.emptyList();
        }
    }

    public void setCache(final TreeCache cache) {
        this.cache = cache;
    }
}
