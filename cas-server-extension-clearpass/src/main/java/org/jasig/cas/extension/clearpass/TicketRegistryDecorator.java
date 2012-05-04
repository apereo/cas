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
package org.jasig.cas.extension.clearpass;

import java.util.Collection;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.cas.monitor.TicketRegistryState;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.AbstractTicketRegistry;
import org.jasig.cas.ticket.registry.TicketRegistry;

/**
 * Decorator that captures tickets and attempts to map them.
 *
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 1.0.7
 */
public final class TicketRegistryDecorator extends AbstractTicketRegistry implements TicketRegistryState {

    private final Log log = LogFactory.getLog(getClass());

    @NotNull
    private final TicketRegistry ticketRegistry;

    private final TicketRegistryState ticketRegistryState;

    @NotNull
    private Map<String,String> cache;

    public TicketRegistryDecorator(final TicketRegistry ticketRegistry, final Map<String, String> cache) {
        this.ticketRegistry = ticketRegistry;
        this.cache = cache;
        this.ticketRegistryState = (TicketRegistryState) ticketRegistry;
    }

    public void addTicket(final Ticket ticket) {
        if (ticket instanceof TicketGrantingTicket) {
            final TicketGrantingTicket ticketGrantingTicket = (TicketGrantingTicket) ticket;
            final String ticketId = ticketGrantingTicket.getId();
            final String userName = ticketGrantingTicket.getAuthentication().getPrincipal().getId();

            log.debug(String.format("Creating mapping ticket [%s] to user name [%s]", ticketId, userName));

            this.cache.put(ticketId, userName);
        }

        this.ticketRegistry.addTicket(ticket);
    }

    public Ticket getTicket(final String ticketId) {
        return this.ticketRegistry.getTicket(ticketId);
    }

    public boolean deleteTicket(final String ticketId) {
        final String userName = this.cache.get(ticketId);

        if (userName != null) {
            log.debug(String.format("Removing mapping ticket [%s] to user name [%s]", ticketId, userName));
            this.cache.remove(userName);
        }

        return this.ticketRegistry.deleteTicket(ticketId);
    }

    public Collection<Ticket> getTickets() {
        return this.ticketRegistry.getTickets();
    }

    public int sessionCount() {
        return this.ticketRegistryState.sessionCount();
    }

    public int serviceTicketCount() {
        return this.ticketRegistryState.serviceTicketCount();
    }
}
