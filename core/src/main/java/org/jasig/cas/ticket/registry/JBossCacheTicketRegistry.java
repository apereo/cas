/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jasig.cas.ticket.Ticket;
import org.jboss.cache.CacheException;
import org.jboss.cache.Node;
import org.jboss.cache.TreeCache;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * Implementation of TicketRegistry that is backed by a JBoss TreeCache.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.5
 *
 */
public final class JBossCacheTicketRegistry extends AbstractTicketRegistry implements InitializingBean {
    
    /** Indicator of what tree branch to put tickets in. */
    private static final String FQN_TICKET = "ticket";

    /** Instance of JBoss TreeCache. */
    private TreeCache cache;
    
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

    public Ticket getTicket(final String ticketId) {
        try {
            if (log.isDebugEnabled()){
                log.debug("Retrieving ticket from registry for: " + ticketId);
            }
            return (Ticket) this.cache.get(FQN_TICKET, ticketId);
        } catch (final CacheException e) {
            log.error(e, e);
            return null;
        }
    }

    public Collection getTickets() {
        try {
            final Node node = this.cache.get(FQN_TICKET);

            if (node == null) {
                return Collections.EMPTY_LIST;
            }
            
            final Set keys = node.getDataKeys();
            final List list = new ArrayList();
            synchronized (this.cache) {
                for (final Iterator iter = keys.iterator(); iter.hasNext();) {
                    list.add(node.get(iter.next()));
                }
            }
            return list;
        } catch (final CacheException e) {
            return Collections.EMPTY_LIST;
        }
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.cache, "cache cannot be null.");
    }

    public void setCache(final TreeCache cache) {
        this.cache = cache;
    }

}
