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

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
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
    
    private Ticket getProxiedTicketInstance(final Ticket ticket) {
        if (ticket instanceof ServiceTicket) {
            return new ProxiedServiceTicket((ServiceTicket) ticket, this.cache);
        }
        
        return new ProxiedTicketGrantingTicket((TicketGrantingTicket) ticket, this.cache);
    }
    
    private final class ProxiedServiceTicket implements ServiceTicket {

        /** Unique id for serialization */
        private static final long serialVersionUID = -8212338455270445676L;

        private final ServiceTicket serviceTicket;

        private final TreeCache cache;

        protected ProxiedServiceTicket(final ServiceTicket serviceTicket,
            final TreeCache cache) {
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
            try {
                this.cache.put(FQN_TICKET, this.serviceTicket.getId(), this.serviceTicket);
            } catch (final CacheException e) {
                throw new RuntimeException(e);
            }
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

        private final TreeCache cache;

        protected ProxiedTicketGrantingTicket(
            final TicketGrantingTicket ticket, final TreeCache cache) {
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
            try {
                this.cache.put(FQN_TICKET, this.ticket.getId(), this.ticket);
            } catch (final CacheException e) {
                throw new RuntimeException(e);
            }
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
            try {
                this.cache.put(FQN_TICKET, this.ticket.getId(), this.ticket);
            } catch (final CacheException e) {
                throw new RuntimeException(e);
            }
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
