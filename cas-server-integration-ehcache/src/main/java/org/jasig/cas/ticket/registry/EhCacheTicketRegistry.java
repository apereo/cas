/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
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

import java.util.Collection;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.jasig.cas.monitor.SessionMonitor;
import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.style.ToStringCreator;

/**
 * <p>
 * <a href="http://ehcache.org/">Ehcache</a> based distributed ticket registry.
 * </p>
 * <p>
 * Use distinct caches for ticket granting tickets (TGT) and service tickets (ST) for:
 * <ul>
 * <li>Tuning : use cache level time to live with different values for TGT an ST.</li>
 * <li>Monitoring : follow separately the number of TGT and ST.</li>
 * <ul>
 * </p>
 * 
 * @author <a href="mailto:cleclerc@xebia.fr">Cyrille Le Clerc</a>
 * @author Adam Rybicki
 * @author Andrew Tillinghast
 */
public final class EhCacheTicketRegistry extends AbstractDistributedTicketRegistry implements InitializingBean {
   
    private Cache serviceTicketsCache  = null;
    
    private Cache ticketGrantingTicketsCache = null;
  
    /** @see #setSupportRegistryState(boolean)*/
    private boolean supportRegistryState = true;
    
    public EhCacheTicketRegistry() {}
    
    public EhCacheTicketRegistry(final Cache serviceTicketsCache, final Cache ticketGrantingTicketsCache) {
      super();
      setServiceTicketsCache(serviceTicketsCache);
      setTicketGrantingTicketsCache(ticketGrantingTicketsCache);
    }
    
    public EhCacheTicketRegistry(final Cache serviceTicketsCache, final Cache ticketGrantingTicketsCache, final boolean supportRegistryState) {
      this(serviceTicketsCache, ticketGrantingTicketsCache);
      setSupportRegistryState(supportRegistryState);
    }
    
    public void addTicket(final Ticket ticket) {
        final Element element = new Element(ticket.getId(), ticket);
        if (ticket instanceof ServiceTicket) {
            log.debug("Adding service ticket {} to the cache", ticket.getId(), this.serviceTicketsCache.getName());
            this.serviceTicketsCache.put(element);
        } else if (ticket instanceof TicketGrantingTicket) {
            log.debug("Adding ticket granting ticket {} to the cache {}", ticket.getId(), this.ticketGrantingTicketsCache.getName());
            this.ticketGrantingTicketsCache.put(element);
        } else {
            throw new IllegalArgumentException("Invalid ticket type " + ticket);
        }
    }
    
    public boolean deleteTicket(final String ticketId) {
        if (StringUtils.isBlank(ticketId)) {
            return false;
        }
        return this.serviceTicketsCache.remove(ticketId) || this.ticketGrantingTicketsCache.remove(ticketId);
    }
    
    public Ticket getTicket(final String ticketId) {
        if (ticketId == null) {
            return null;
        }

        Element element = this.serviceTicketsCache.get(ticketId);
        if (element == null) {
            element = this.ticketGrantingTicketsCache.get(ticketId);
        }
        return element == null ? null : getProxiedTicketInstance((Ticket)element.getValue());
    }
    
    public Collection<Ticket> getTickets() {
        throw new UnsupportedOperationException("GetTickets not supported.");
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
    protected boolean needsCallback(){
    	return false;
    }

    /** 
     * Flag to indicate whether this registry instance should participate in reporting its state with default value set to <code>true</code>.
     * Based on the <a href="http://ehcache.org/apidocs/net/sf/ehcache/Ehcache.html#getKeysWithExpiryCheck()">EhCache documentation</a>, 
     * determining the number of service tickets and the total session count from the cache can be considered an expensive operation with the 
     * time taken as O(n), where n is the number of elements in the cache. 
     * 
     * <p>Therefore, the flag provides a level of flexibility such that depending on the cache and environment settings, reporting statistics
     * can be set to false and disabled.</p>
     *  
     * @see #sessionCount()
     * @see #serviceTicketCount()
     * @see SessionMonitor
     */
    public void setSupportRegistryState(final boolean supportRegistryState) {
      this.supportRegistryState = supportRegistryState;
    }
    
    public void afterPropertiesSet() throws Exception {
      if (this.serviceTicketsCache == null || this.ticketGrantingTicketsCache == null) {
        throw new BeanInstantiationException(this.getClass(), "Both serviceTicketsCache and ticketGrantingTicketsCache are required properties.");
      }
      
      if (this.log.isDebugEnabled()) {
        CacheConfiguration config = this.serviceTicketsCache.getCacheConfiguration();
        log.debug("serviceTicketsCache.maxElementsInMemory={}", config.getMaxEntriesLocalHeap());
        log.debug("serviceTicketsCache.maxElementsOnDisk={}", config.getMaxElementsOnDisk());
        log.debug("serviceTicketsCache.overflowToDisk={}", config.isOverflowToDisk());
        log.debug("serviceTicketsCache.timeToLive={}", config.getTimeToLiveSeconds());
        log.debug("serviceTicketsCache.timeToIdle={}", config.getTimeToIdleSeconds());
  
        config = this.ticketGrantingTicketsCache.getCacheConfiguration();
        log.debug("ticketGrantingTicketsCache.maxElementsInMemory={}", config.getMaxEntriesLocalHeap());
        log.debug("ticketGrantingTicketsCache.maxElementsOnDisk={}", config.getMaxElementsOnDisk());
        log.debug("ticketGrantingTicketsCache.overflowToDisk={}", config.isOverflowToDisk());
        log.debug("ticketGrantingTicketsCache.timeToLive={}", config.getTimeToLiveSeconds());
        log.debug("ticketGrantingTicketsCache.timeToIdle={}", config.getTimeToIdleSeconds());
      } 
    }

    /**
     * @see Cache#getKeysNoDuplicateCheck()
     */
    public int sessionCount() {
        return BooleanUtils.toInteger(this.supportRegistryState, this.ticketGrantingTicketsCache.getKeysWithExpiryCheck().size() , super.sessionCount());
    }

    /**
     * @see Cache#getKeysNoDuplicateCheck()
     */
    public int serviceTicketCount() {
        return BooleanUtils.toInteger(this.supportRegistryState, this.serviceTicketsCache.getKeysWithExpiryCheck().size() , super.serviceTicketCount());
    }
}