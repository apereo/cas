/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.registry.support;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.registry.AbstractTicketRegistry;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * Ticket registry backed by EHCache caching subsystem.
 * <p>
 * Note: assumes that <code>Cache</code> instances will be injected via setter
 * method, typically by some kind of IoC container.
 * 
 * @author Dmitriy Kopylenko
 * @version $Revision$ $Date$
 * @since 3.0
 */
public final class EhCacheTicketRegistry extends AbstractTicketRegistry implements
    InitializingBean {

    /** The instance of an EhCache cache. */
    private Cache cache;

    /**
     * Set backed cache.
     */
    public void setCache(final Cache cache) {
        this.cache = cache;
    }

    /**
     * @throws IllegalArgumentException if the ticket is null.
     */
    public void addTicket(final Ticket ticket) {
        Assert.notNull(ticket);

        if (log.isDebugEnabled()) {
            log.debug("Added ticket [" + ticket.getId() + "] to registry.");
        }
        this.cache.put(new Element(ticket.getId(), ticket));
    }

    /**
     * @throws IllegalStateException if the cache throws an exception.
     */
    public Ticket getTicket(final String ticketId) {
        if (log.isDebugEnabled()) {
            log.debug("Attempting to retrieve ticket [" + ticketId + "]");
        }
        if (ticketId == null) {
            return null;
        }

        try {
            final Element element = this.cache.get(ticketId);
            if (element == null) {
                return null;
            }

            final Ticket ticket = (Ticket) element.getValue();

            if (log.isDebugEnabled()) {
                log.debug("Ticket [" + ticketId + "] found in registry.");
            }

            return ticket;
        } catch (final Exception ex) {
            final IllegalStateException ise = new IllegalStateException();
            ise.initCause(ex);
            throw ise;
        }
    }

    public boolean deleteTicket(final String ticketId) {
        if (log.isDebugEnabled()) {
            log.debug("Removing ticket [" + ticketId + "] from registry");
        }
        return this.cache.remove(ticketId);
    }

    /**
     * @throws IllegalStateException if the backing cache is not ready.
     */
    public Collection getTickets() {
        try {
            final List keys = this.cache.getKeys();
            final Collection items = new ArrayList();

            for (final Iterator iter = keys.iterator(); iter.hasNext();) {
                final Serializable key = (Serializable) iter.next();
                final Element element = this.cache.get(key);
                items.add(element.getValue());
            }
            return Collections.unmodifiableCollection(items);
        } catch (Exception e) {
            final IllegalStateException ise = new IllegalStateException();
            ise.initCause(e);
            throw ise;
        }
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.cache, "cache cannot be null.");
    }
}
