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

package org.jasig.cas.ticket.registry.encrypt;


import org.jasig.cas.monitor.TicketRegistryState;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.registry.AbstractTicketRegistry;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.util.CipherExecutor;
import org.jasig.cas.util.CompressionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Misagh Moayyed
 * @since 4.1
 */
public final class CrypticTicketRegistry extends AbstractTicketRegistry {

    /** Logger instance. */
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @NotNull
    private final CipherExecutor cipherExecutor;

    /** The real instance of the ticket registry that is to be decorated. */
    @NotNull
    private final TicketRegistry ticketRegistry;

    /**
     * Instantiates a new Cryptic ticket registry.
     *
     * @param actualTicketRegistry the actual ticket registry
     * @param cipherExecutor the cipher executor
     */
    public CrypticTicketRegistry(final TicketRegistry actualTicketRegistry, final CipherExecutor cipherExecutor) {
        this.ticketRegistry = actualTicketRegistry;
        this.cipherExecutor = cipherExecutor;
    }

    @Override
    public void addTicket(final Ticket ticket) {
        final Ticket encodedTicket = encodeTicket(ticket);
        this.ticketRegistry.addTicket(encodedTicket);
    }

    @Override
    public Ticket getTicket(final String ticketId) {
        final String encodedId = CompressionUtils.encodeObject(ticketId.getBytes(), cipherExecutor);
        final Ticket ticket = this.ticketRegistry.getTicket(ticketId);
        return decodeTicket(ticket);
    }

    @Override
    public boolean deleteTicket(final String ticketId) {
        final String encodedId = CompressionUtils.encodeObject(ticketId.getBytes(), cipherExecutor);
        return this.ticketRegistry.deleteTicket(encodedId);
    }

    @Override
    public Collection<Ticket> getTickets() {
        final Collection<Ticket> col = this.ticketRegistry.getTickets();
        return decodeTickets(col);
    }

    @Override
    public int sessionCount() {
        return ((TicketRegistryState) this.ticketRegistry).sessionCount();
    }

    @Override
    public int serviceTicketCount() {
        return ((TicketRegistryState) this.ticketRegistry).serviceTicketCount();
    }

    /**
     * Encode ticket.
     *
     * @param ticket the ticket
     * @return the ticket
     */
    private Ticket encodeTicket(final Ticket ticket)  {
        if (ticket == null) {
            return null;
        }

        logger.info("Encoding [{}]", ticket);
        final String encodedTicketObject = CompressionUtils.encodeObject(ticket, cipherExecutor);
        final String encodedTicketId = CompressionUtils.encodeObject(ticket.getId().getBytes(), this.cipherExecutor);
        final EncodedTicket encodedTicket = new EncodedTicket(ticket, encodedTicketObject, encodedTicketId);
        logger.info("Created [{}]", ticket);
        return encodedTicket;
    }

    /**
     * Decode ticket.
     *
     * @param result the result
     * @return the ticket
     */
    private Ticket decodeTicket(final Ticket result) {
        if (result == null) {
            return null;
        }

        logger.info("Attempting to decode [{}]",  result);
        final EncodedTicket encodedTicket = (EncodedTicket) result;

        final Ticket ticket = CompressionUtils.decodeObject(encodedTicket.getEncoded(),
                this.cipherExecutor, Ticket.class);
        logger.info("Decoded [{}]",  ticket);
        return ticket;
    }

    /**
     * Decode tickets.
     *
     * @param items the items
     * @return the set
     */
    private Collection<Ticket>  decodeTickets(final Collection<Ticket> items) {
        if (items == null || items.isEmpty()) {
            return items;
        }

        final Set<Ticket> tickets = new HashSet<>(items.size());
        for (final Ticket item : items) {
            final EncodedTicket encodedTicket = (EncodedTicket) item;
            final Ticket ticket = CompressionUtils.decodeObject(encodedTicket.getEncoded(),
                    this.cipherExecutor, Ticket.class);
            logger.info("Decoded [{}]",  ticket);
            tickets.add(ticket);
        }
        return tickets;
    }
}
