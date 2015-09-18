/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.ticket.registry;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.encrypt.AbstractCrypticTicketRegistry;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.style.ToStringCreator;

import java.util.Collection;
import java.util.HashSet;

/**
 * <p>
 * <a href="http://ehcache.org/">Ehcache</a> based distributed ticket registry.
 * </p>
 *
 * <p>
 * Use distinct caches for ticket granting tickets (TGT) and service tickets (ST) for:
 * <ul>
 *   <li>Tuning : use cache level time to live with different values for TGT an ST.</li>
 *   <li>Monitoring : follow separately the number of TGT and ST.</li>
 * </ul>
 *
 * @author <a href="mailto:cleclerc@xebia.fr">Cyrille Le Clerc</a>
 * @author Adam Rybicki
 * @author Andrew Tillinghast
 * @since 3.5
 */
public final class EhCacheTicketRegistry extends AbstractCrypticTicketRegistry implements InitializingBean {

    private Cache serviceTicketsCache;

    private Cache ticketGrantingTicketsCache;

    /**
     * @see #setSupportRegistryState(boolean)
     **/
    private boolean supportRegistryState = true;

    /**
     * Instantiates a new EhCache ticket registry.
     */
    public EhCacheTicketRegistry() {
        super();
    }

    /**
     * Instantiates a new EhCache ticket registry.
     *
     * @param serviceTicketsCache the service tickets cache
     * @param ticketGrantingTicketsCache the ticket granting tickets cache
     */
    public EhCacheTicketRegistry(final Cache serviceTicketsCache, final Cache ticketGrantingTicketsCache) {
        super();
        setServiceTicketsCache(serviceTicketsCache);
        setTicketGrantingTicketsCache(ticketGrantingTicketsCache);
    }

    /**
     * Instantiates a new EhCache ticket registry.
     *
     * @param serviceTicketsCache the service tickets cache
     * @param ticketGrantingTicketsCache the ticket granting tickets cache
     * @param supportRegistryState the support registry state
     */
    public EhCacheTicketRegistry(final Cache serviceTicketsCache, final Cache ticketGrantingTicketsCache,
            final boolean supportRegistryState) {
        this(serviceTicketsCache, ticketGrantingTicketsCache);
        setSupportRegistryState(supportRegistryState);
    }

    @Override
    public void addTicket(final Ticket ticketToAdd) {
        final Ticket ticket = encodeTicket(ticketToAdd);
        final Element element = new Element(ticket.getId(), ticket);
        if (ticket instanceof ServiceTicket) {
            logger.debug("Adding service ticket {} to the cache {}", ticket.getId(), this.serviceTicketsCache.getName());
            this.serviceTicketsCache.put(element);
        } else if (ticket instanceof TicketGrantingTicket) {
            logger.debug("Adding ticket granting ticket {} to the cache {}", ticket.getId(),
                    this.ticketGrantingTicketsCache.getName());
            this.ticketGrantingTicketsCache.put(element);
        } else {
            throw new IllegalArgumentException("Invalid ticket type " + ticket);
        }
    }

    @Override
    public boolean deleteTicket(final String ticketIdToDelete) {
        final String ticketId = encodeTicketId(ticketIdToDelete);
        if (StringUtils.isBlank(ticketId)) {
            return false;
        }
        return this.serviceTicketsCache.remove(ticketId) || this.ticketGrantingTicketsCache.remove(ticketId);
    }

    @Override
    public Ticket getTicket(final String ticketIdToGet) {
        final String ticketId = encodeTicketId(ticketIdToGet);
        if (ticketId == null) {
            return null;
        }

        Element element = this.serviceTicketsCache.get(ticketId);
        if (element == null) {
            element = this.ticketGrantingTicketsCache.get(ticketId);
        }
        if (element == null) {
            logger.debug("No ticket by id [{}] is found in the registry", ticketId);
            return null;
        }

        final Ticket ticket = decodeTicket((Ticket) element.getObjectValue());
        return getProxiedTicketInstance(ticket);
    }

    @Override
    public Collection<Ticket> getTickets() {
        final Collection<Element> serviceTickets = this.serviceTicketsCache.getAll(
                this.serviceTicketsCache.getKeysWithExpiryCheck()).values();
        final Collection<Element> tgtTicketsTickets = this.ticketGrantingTicketsCache.getAll(
                this.ticketGrantingTicketsCache.getKeysWithExpiryCheck()).values();

        final Collection<Ticket> allTickets = new HashSet<>(serviceTickets.size() + tgtTicketsTickets.size());

        for (final Element ticket : serviceTickets) {
            allTickets.add((Ticket) ticket.getObjectValue());
        }

        for (final Element ticket : tgtTicketsTickets) {
            allTickets.add((Ticket) ticket.getObjectValue());
        }

        return decodeTickets(allTickets);
    }

    public void setServiceTicketsCache(final Cache serviceTicketsCache) {
        this.serviceTicketsCache = serviceTicketsCache;
    }

    public void setTicketGrantingTicketsCache(final Cache ticketGrantingTicketsCache) {
        this.ticketGrantingTicketsCache = ticketGrantingTicketsCache;
    }

    @Override
    public String toString() {
        return new ToStringCreator(this).append("ticketGrantingTicketsCache", this.ticketGrantingTicketsCache)
                .append("serviceTicketsCache", this.serviceTicketsCache).toString();
    }

    @Override
    protected void updateTicket(final Ticket ticket) {
        addTicket(ticket);
    }

    @Override
    protected boolean needsCallback() {
        return false;
    }

    /**
     * Flag to indicate whether this registry instance should participate in reporting its state with
     * default value set to <code>true</code>.
     * Based on the <a href="http://ehcache.org/apidocs/net/sf/ehcache/Ehcache.html#getKeysWithExpiryCheck()">EhCache documentation</a>,
     * determining the number of service tickets and the total session count from the cache can be considered
     * an expensive operation with the time taken as O(n), where n is the number of elements in the cache.
     *
     * <p>Therefore, the flag provides a level of flexibility such that depending on the cache and environment
     * settings, reporting statistics
     * can be set to false and disabled.</p>
     *
     * @param supportRegistryState true, if the registry is to support registry state
     * @see #sessionCount()
     * @see #serviceTicketCount()
     * @see org.jasig.cas.monitor.SessionMonitor
     */
    public void setSupportRegistryState(final boolean supportRegistryState) {
        this.supportRegistryState = supportRegistryState;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.serviceTicketsCache == null || this.ticketGrantingTicketsCache == null) {
            throw new BeanInstantiationException(this.getClass(),
                    "Both serviceTicketsCache and ticketGrantingTicketsCache are required properties.");
        }

        if (logger.isDebugEnabled()) {
            CacheConfiguration config = this.serviceTicketsCache.getCacheConfiguration();
            logger.debug("serviceTicketsCache.maxElementsInMemory={}", config.getMaxEntriesLocalHeap());
            logger.debug("serviceTicketsCache.maxElementsOnDisk={}", config.getMaxElementsOnDisk());
            logger.debug("serviceTicketsCache.isOverflowToDisk={}", config.isOverflowToDisk());
            logger.debug("serviceTicketsCache.timeToLive={}", config.getTimeToLiveSeconds());
            logger.debug("serviceTicketsCache.timeToIdle={}", config.getTimeToIdleSeconds());
            logger.debug("serviceTicketsCache.cacheManager={}", this.serviceTicketsCache.getCacheManager().getName());

            config = this.ticketGrantingTicketsCache.getCacheConfiguration();
            logger.debug("ticketGrantingTicketsCache.maxElementsInMemory={}", config.getMaxEntriesLocalHeap());
            logger.debug("ticketGrantingTicketsCache.maxElementsOnDisk={}", config.getMaxElementsOnDisk());
            logger.debug("ticketGrantingTicketsCache.isOverflowToDisk={}", config.isOverflowToDisk());
            logger.debug("ticketGrantingTicketsCache.timeToLive={}", config.getTimeToLiveSeconds());
            logger.debug("ticketGrantingTicketsCache.timeToIdle={}", config.getTimeToIdleSeconds());
            logger.debug("ticketGrantingTicketsCache.cacheManager={}", this.ticketGrantingTicketsCache.getCacheManager()
                    .getName());
        }
    }

    /**
     * {@inheritDoc}
     * @see Cache#getKeysWithExpiryCheck()
     */
    @Override
    public int sessionCount() {
        return BooleanUtils.toInteger(this.supportRegistryState, this.ticketGrantingTicketsCache
                .getKeysWithExpiryCheck().size(), super.sessionCount());
    }

    /**
     * {@inheritDoc}
     * @see Cache#getKeysWithExpiryCheck()
     */
    @Override
    public int serviceTicketCount() {
        return BooleanUtils.toInteger(this.supportRegistryState, this.serviceTicketsCache.getKeysWithExpiryCheck()
                .size(), super.serviceTicketCount());
    }
}
