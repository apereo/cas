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

import org.jasig.cas.ticket.Ticket;
import org.jboss.cache.Cache;
import org.jboss.cache.CacheException;
import org.jboss.cache.Node;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @deprecated As of 4.1 the Jboss cache integration module is no longer supported.
 * Please use other means of configuring your distributed ticket registry, such as
 * ehcache or memcached integrations with CAS.
 *
 * <p>Implementation of TicketRegistry that is backed by a JBoss TreeCache.
 * @author Scott Battaglia
 * @since 3.1
 */
@Deprecated
public final class JBossCacheTicketRegistry extends AbstractDistributedTicketRegistry {

    /** Indicator of what tree branch to put tickets in. */
    private static final String FQN_TICKET = "ticket";

    /** Instance of JBoss TreeCache. */
    @NotNull
    private Cache<String, Ticket> cache;

    @Override
    protected void updateTicket(final Ticket ticket) {
        try {
            this.cache.put(FQN_TICKET, ticket.getId(), ticket);
        } catch (final CacheException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addTicket(final Ticket ticket) {
        try {
            logger.debug("Adding ticket to registry for: {}", ticket.getId());
            this.cache.put(FQN_TICKET, ticket.getId(), ticket);
        } catch (final CacheException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean deleteTicket(final String ticketId) {
        try {
            logger.debug("Removing ticket from registry for: {}", ticketId);
            return this.cache.remove(FQN_TICKET, ticketId) != null;
        } catch (final CacheException e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     * Returns a proxied instance.
     *
     * @see org.jasig.cas.ticket.registry.TicketRegistry#getTicket(java.lang.String)
     */
    @Override
    public Ticket getTicket(final String ticketId) {
        try {
            logger.debug("Retrieving ticket from registry for: {}", ticketId);
            return getProxiedTicketInstance(this.cache.get(FQN_TICKET, ticketId));
        } catch (final CacheException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Collection<Ticket> getTickets() {
        try {
            final Node<String, Ticket> node = this.cache.getNode(FQN_TICKET);

            if (node == null) {
                return Collections.emptyList();
            }

            final Set<String> keys = node.getKeys();
            final List<Ticket> list = new ArrayList<>();

            for (final String key : keys) {

                /**  Returns null if the node contains no mapping for this key. **/
                final Ticket ticket = node.get(key);

                if (ticket != null) {
                    list.add(node.get(key));
                }
            }

            return list;
        } catch (final CacheException e) {
            return Collections.emptyList();
        }
    }

    public void setCache(final Cache<String, Ticket> cache) {
        this.cache = cache;
    }

    @Override
    protected boolean needsCallback() {
        return true;
    }
}
