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
package org.jasig.cas.extension.clearpass;

import org.jasig.cas.monitor.TicketRegistryState;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.AbstractTicketRegistry;
import org.jasig.cas.ticket.registry.TicketRegistry;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Map;

/**
 * Decorator that captures tickets and attempts to map them.
 *
 * @deprecated As of 4.1, use {@link org.jasig.cas.authentication.CacheCredentialsMetaDataPopulator} instead.
 * @author Scott Battaglia
 * @since 1.0.7
 */
@Deprecated
public final class TicketRegistryDecorator extends AbstractTicketRegistry {

    /** The real instance of the ticket registry that is to be decorated. */
    @NotNull
    private final TicketRegistry ticketRegistry;

    /** Map instance where credentials are stored. */
    @NotNull
    private final Map<String, String> cache;

    /**
     * Constructs an instance of the decorator wrapping the real ticket registry instance inside.
     *
     * @param actualTicketRegistry The real instance of the ticket registry that is to be decorated
     * @param cache Map instance where credentials are stored.
     *
     * @see EhcacheBackedMap
     */
    public TicketRegistryDecorator(final TicketRegistry actualTicketRegistry, final Map<String, String> cache) {
        this.ticketRegistry = actualTicketRegistry;
        this.cache = cache;
    }

    @Override
    public void addTicket(final Ticket ticket) {
        if (ticket instanceof TicketGrantingTicket) {
            final TicketGrantingTicket ticketGrantingTicket = (TicketGrantingTicket) ticket;
            final String ticketId = ticketGrantingTicket.getId();
            final String userName = ticketGrantingTicket.getAuthentication().getPrincipal().getId().toLowerCase();

            logger.debug("Creating mapping ticket {} to user name {}", ticketId, userName);

            this.cache.put(ticketId, userName);
        }

        this.ticketRegistry.addTicket(ticket);
    }

    @Override
    public Ticket getTicket(final String ticketId) {
        return this.ticketRegistry.getTicket(ticketId);
    }

    @Override
    public boolean deleteTicket(final String ticketId) {
        final String userName = this.cache.get(ticketId);

        if (userName != null) {
            logger.debug("Removing mapping ticket {} for user name {}", ticketId, userName);
            this.cache.remove(userName);
        }

        return this.ticketRegistry.deleteTicket(ticketId);
    }

    @Override
    public Collection<Ticket> getTickets() {
        return this.ticketRegistry.getTickets();
    }

    @Override
    public int sessionCount() {
        if (this.ticketRegistry instanceof TicketRegistryState) {
            return ((TicketRegistryState) this.ticketRegistry).sessionCount();
        }
        logger.debug("Ticket registry {} does not report the sessionCount() operation of the registry state.",
                this.ticketRegistry.getClass().getName());
        return super.sessionCount();
    }

    @Override
    public int serviceTicketCount() {
        if (this.ticketRegistry instanceof TicketRegistryState) {
            return ((TicketRegistryState) this.ticketRegistry).serviceTicketCount();
        }
        logger.debug("Ticket registry {} does not report the serviceTicketCount() operation of the registry state.",
                this.ticketRegistry.getClass().getName());
        return super.serviceTicketCount();
    }
}
