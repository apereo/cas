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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.jasig.cas.ticket.ServiceTicket;
import org.jasig.cas.ticket.Ticket;
import org.jboss.cache.Cache;
import org.jboss.cache.CacheException;
import org.jboss.cache.Node;

/**
 * Implementation of TicketRegistry that is backed by a JBoss TreeCache.
 * <p />
 * 
 * Optional possibility to use distinct caches for ticket granting tickets (TGT)
 * and service tickets (ST) for:
 * <ul>
 * <li>Tuning : use cache level time to live with different values for TGT an
 * ST.</li>
 * <li>Monitoring : follow separately the number of TGT and ST.</li>
 * <ul>
 * </p>
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.1
 * 
 */
public class JBossCacheTicketRegistry extends AbstractDistributedTicketRegistry {

	/** Indicator of what tree branch to put tickets in. */
	static final String FQN_TICKET = "ticket";
	static final String FQN_SERVICE_TICKET = "serviceticket";

	@NotNull
	private Cache<String, Ticket> ticketGrantingTicketsCache;

	@NotNull
	private Cache<String, Ticket> serviceTicketsCache;

	public JBossCacheTicketRegistry() {
		super();
	}

	protected void updateTicket(Ticket ticket) {
		try {
			getCacheForTicketType(ticket).put(getFqn(ticket), ticket.getId(), ticket);
		} catch (final CacheException e) {
			throw new RuntimeException(e);
		}
	}

	public void addTicket(final Ticket ticket) {
		try {
			log.debug("Adding ticket to registry for: {}", ticket.getId());
			getCacheForTicketType(ticket).put(getFqn(ticket), ticket.getId(), ticket);
		} catch (final CacheException e) {
			log.error("Problems while adding ticket to cache.", e);
			throw new RuntimeException(e);
		}
	}

	String getFqn(final Ticket ticket) {
		if (ticket instanceof ServiceTicket) {
			return FQN_SERVICE_TICKET;
		}
		return FQN_TICKET;
	}

	Cache<String, Ticket> getCacheForTicketType(final Ticket ticket) {
		if (ticket instanceof ServiceTicket) {
			return this.serviceTicketsCache;
		}
		return this.ticketGrantingTicketsCache;
	}

	public boolean deleteTicket(final String ticketId) {
		try {
			log.debug("Delete Ticket {} from registry.", ticketId);
			return this.serviceTicketsCache.remove(FQN_SERVICE_TICKET, ticketId) != null
					|| this.ticketGrantingTicketsCache.remove(FQN_TICKET, ticketId) != null;
		} catch (final CacheException e) {
			log.error("Error while deleting ticket from cache.", e);
			return false;
		}
	}

	public Ticket getTicket(final String ticketId) {
		try {
			log.debug("Retrieving ticket from registry for: {}", ticketId);

			Ticket ticket = this.serviceTicketsCache.get(FQN_SERVICE_TICKET, ticketId);
			if (ticket == null) {
				ticket = this.ticketGrantingTicketsCache.get(FQN_TICKET, ticketId);
			}
			return getProxiedTicketInstance(ticket);
		} catch (final CacheException e) {
			log.error("Error while reading the tickets.", e);
			return null;
		}
	}

	public Collection<Ticket> getTickets() {
		try {
			final Node<String, Ticket> tgtNodes = this.ticketGrantingTicketsCache.getNode(FQN_TICKET);
			final Node<String, Ticket> serviceNodes = this.serviceTicketsCache.getNode(FQN_SERVICE_TICKET);

			final List<Ticket> list = new ArrayList<Ticket>();

			addTicketsToList(tgtNodes, list);
			addTicketsToList(serviceNodes, list);

			return list;
		} catch (final CacheException e) {
			return Collections.emptyList();
		}
	}

	void addTicketsToList(final Node<String, Ticket> node, final List<Ticket> list) {
		if (node != null) {
			for (final String key : node.getKeys()) {
				Ticket ticket = node.get(key);
				if (ticket != null) {
					list.add(ticket);
				}
			}
		}
	}

	@Override
	protected boolean needsCallback() {
		return false;
	}

	public Cache<String, Ticket> getServiceTicketsCache() {
		return serviceTicketsCache;
	}

	public void setServiceTicketsCache(Cache<String, Ticket> serviceTicketsCache) {
		this.serviceTicketsCache = serviceTicketsCache;
	}

	public void setTicketGrantingTicketsCache(Cache<String, Ticket> ticketGrantingTicketsCache) {
		this.ticketGrantingTicketsCache = ticketGrantingTicketsCache;
		if (getServiceTicketsCache() == null) {
			setServiceTicketsCache(ticketGrantingTicketsCache);
		}
	}

	/**
	 * Setter for the ticket granting ticket cache. Keeping this method for
	 * backward-compatibility.
	 * 
	 * @param ticketGrantingTicketsCache
	 */
	public void setCache(Cache<String, Ticket> ticketGrantingTicketsCache) {
		setTicketGrantingTicketsCache(ticketGrantingTicketsCache);
	}
}
