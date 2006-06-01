/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.registry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * Implementation of TicketRegistry that uses two EhCaches (one for
 * TicketGrantingTickets and one for ServiceTickets). Properly configured, this
 * TicketRegistry can support distributed ticket registries.
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.5
 */
public final class EhCacheTicketRegistry implements TicketRegistry,
    InitializingBean {

    /** Caching instance for caching TicketGrantingTickets */
    private Cache ticketGrantingTicketCache;

    /** Caching instance for caching ServiceTickets */
    private Cache serviceTicketCache;

    public void setServiceTicketCache(final Cache serviceTicketCache) {
        this.serviceTicketCache = serviceTicketCache;
    }

    public void setTicketGrantingTicketCache(
        final Cache ticketGrantingTicketCache) {
        this.ticketGrantingTicketCache = ticketGrantingTicketCache;
    }

    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.ticketGrantingTicketCache,
            "ticketGrantingTicketCache cannot be null");
        Assert.notNull(this.serviceTicketCache,
            "serviceTicketCache cannot be null");
    }

    public void addTicket(final Ticket ticket) {
        if (ServiceTicket.class.isAssignableFrom(ticket.getClass())) {
            this.serviceTicketCache.put(new Element(ticket.getId(), ticket));
        }
        if (TicketGrantingTicket.class.isAssignableFrom(ticket.getClass())) {
            this.ticketGrantingTicketCache.put(new Element(ticket.getId(),
                ticket));
        }
    }

    public Ticket getTicket(final String ticketId, final Class clazz) {
        final Element element = this.serviceTicketCache.get(ticketId);

        if (element != null) {
            final ServiceTicket ticket = (ServiceTicket) element.getValue();

            if (!ServiceTicket.class.equals(clazz)) {
                throw new ClassCastException();
            }

            return new ProxiedServiceTicket(ticket, this.serviceTicketCache);
        }

        final Element tgtElement = this.ticketGrantingTicketCache.get(ticketId);

        if (tgtElement != null) {
            final TicketGrantingTicket ticket = (TicketGrantingTicket) tgtElement
                .getValue();

            if (!TicketGrantingTicket.class.equals(clazz)) {
                throw new ClassCastException();
            }

            return new ProxiedTicketGrantingTicket(ticket,
                this.ticketGrantingTicketCache);
        }

        return null;
    }

    public Ticket getTicket(final String ticketId) {
        final Element element = this.serviceTicketCache.get(ticketId);

        if (element != null) {
            final ServiceTicket ticket = (ServiceTicket) element.getValue();

            return new ProxiedServiceTicket(ticket, this.serviceTicketCache);
        }

        final Element tgtElement = this.ticketGrantingTicketCache.get(ticketId);

        if (tgtElement != null) {
            final TicketGrantingTicket ticket = (TicketGrantingTicket) tgtElement
                .getValue();

            return new ProxiedTicketGrantingTicket(ticket,
                this.ticketGrantingTicketCache);
        }

        return null;
    }

    public boolean deleteTicket(final String ticketId) {
        return this.ticketGrantingTicketCache.remove(ticketId)
            || this.serviceTicketCache.remove(ticketId);
    }

    public synchronized Collection getTickets() {
        final List tickets = new ArrayList();

        for (final Iterator iter = this.ticketGrantingTicketCache.getKeys()
            .iterator(); iter.hasNext();) {
            final Serializable key = (Serializable) iter.next();
            final Element element = this.ticketGrantingTicketCache.get(key);
            tickets.add(element.getValue());
        }

        for (final Iterator iter = this.serviceTicketCache.getKeys().iterator(); iter
            .hasNext();) {
            final Serializable key = (Serializable) iter.next();
            final Element element = this.serviceTicketCache.get(key);
            tickets.add(element.getValue());
        }

        return Collections.unmodifiableCollection(tickets);
    }

    private final class ProxiedServiceTicket implements ServiceTicket {

        /** Unique id for serialization */
        private static final long serialVersionUID = -8212338455270445676L;

        private final ServiceTicket serviceTicket;

        private final Cache cache;

        protected ProxiedServiceTicket(final ServiceTicket serviceTicket,
            final Cache cache) {
            this.serviceTicket = serviceTicket;
            this.cache = cache;
        }

        public Service getService() {
            return this.serviceTicket.getService();
        }

        public TicketGrantingTicket grantTicketGrantingTicket(final String id,
            final Authentication authentication,
            final ExpirationPolicy expirationPolicy) {
            return this.serviceTicket.grantTicketGrantingTicket(id,
                authentication, expirationPolicy);
        }

        public boolean isFromNewLogin() {
            return this.serviceTicket.isFromNewLogin();
        }

        public long getCreationTime() {
            return this.serviceTicket.getCreationTime();
        }

        public TicketGrantingTicket getGrantingTicket() {
            return this.serviceTicket.getGrantingTicket();
        }

        public String getId() {
            return this.serviceTicket.getId();
        }

        public boolean isValidFor(final Service service) {
            final boolean result = this.serviceTicket.isValidFor(service);
            this.cache.put(new Element(this.serviceTicket.getId(),
                this.serviceTicket));
            return result;
        }

        public boolean isExpired() {
            return this.serviceTicket.isExpired();
        }

        public boolean equals(final Object obj) {
            return this.serviceTicket.equals(obj);
        }

        public String toString() {
            return this.serviceTicket.toString();
        }
    }

    private final class ProxiedTicketGrantingTicket implements
        TicketGrantingTicket {

        /** Unique Id for Serializaion */
        private static final long serialVersionUID = -4361481214176025025L;

        private final TicketGrantingTicket ticket;

        private final Cache cache;

        protected ProxiedTicketGrantingTicket(
            final TicketGrantingTicket ticket, final Cache cache) {
            this.ticket = ticket;
            this.cache = cache;
        }

        public long getCreationTime() {
            return this.ticket.getCreationTime();
        }

        public TicketGrantingTicket getGrantingTicket() {
            return this.ticket.getGrantingTicket();
        }

        public String getId() {
            return this.ticket.getId();
        }

        public boolean isExpired() {
            return this.ticket.isExpired();
        }

        public void expire() {
            this.ticket.expire();
            this.cache.put(new Element(this.ticket.getId(), this.ticket));
        }

        public Authentication getAuthentication() {
            return this.ticket.getAuthentication();
        }

        public List getChainedAuthentications() {
            return this.ticket.getChainedAuthentications();
        }

        public ServiceTicket grantServiceTicket(final String id,
            final Service service, final ExpirationPolicy expirationPolicy,
            final boolean credentialsProvided) {
            final ServiceTicket serviceTicket = this.ticket.grantServiceTicket(
                id, service, expirationPolicy, credentialsProvided);
            this.cache.put(new Element(this.ticket.getId(), this.ticket));
            return serviceTicket;
        }

        public boolean isRoot() {
            return this.ticket.isRoot();
        }

        public boolean equals(final Object obj) {
            return this.ticket.equals(obj);
        }

        public String toString() {
            return this.ticket.toString();
        }
    }
}
