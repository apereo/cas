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


import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.registry.AbstractDistributedTicketRegistry;
import org.jasig.cas.util.CipherExecutor;
import org.jasig.cas.util.CompressionUtils;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A distributed ticket registry that is able to encode
 * tickets before they are persisted and decoded back. This should be used, particularly
 * in clustered deployments where replication of tickets is carried out
 * over an insecure network connection. By default, encryption is turned off
 * unless {@link #setCipherExecutor(CipherExecutor)} is configured.
 *
 * @author Misagh Moayyed
 * @since 4.2
 * @see org.jasig.cas.util.JCEBasedCipherExecutor
 */
public abstract class AbstractCrypticTicketRegistry extends AbstractDistributedTicketRegistry {

    @NotNull
    private CipherExecutor<byte[], byte[]> cipherExecutor;

    /**
     * Instantiates a new Cryptic ticket registry.
     */
    public AbstractCrypticTicketRegistry() {
    }

    public void setCipherExecutor(final CipherExecutor<byte[], byte[]> cipherExecutor) {
        this.cipherExecutor = cipherExecutor;
    }

    /**
     * Encode ticket id into a SHA-512.
     *
     * @param ticketId the ticket id
     * @return the ticket
     */
    protected String encodeTicketId(final String ticketId)  {
        if (this.cipherExecutor == null) {
            logger.trace("Ticket encryption is not enabled. Falling back to default behavior");
            return ticketId;
        }
        if (StringUtils.isBlank(ticketId)) {
            return ticketId;
        }

        return CompressionUtils.sha512Hex(ticketId);
    }

    /**
     * Encode ticket.
     *
     * @param ticket the ticket
     * @return the ticket
     */
    protected Ticket encodeTicket(final Ticket ticket)  {
        if (this.cipherExecutor == null) {
            logger.trace("Ticket encryption is not enabled. Falling back to default behavior");
            return ticket;
        }

        if (ticket == null) {
            return ticket;
        }

        logger.info("Encoding [{}]", ticket);
        final byte[] encodedTicketObject = CompressionUtils.serializeAndEncodeObject(
                this.cipherExecutor, ticket);
        final String encodedTicketId = encodeTicketId(ticket.getId());
        final EncodedTicket encodedTicket = new EncodedTicket(
                encodedTicketObject, encodedTicketId);
        logger.info("Created [{}]", encodedTicket);
        return encodedTicket;
    }

    /**
     * Decode ticket.
     *
     * @param result the result
     * @return the ticket
     */
    protected Ticket decodeTicket(final Ticket result) {
        if (this.cipherExecutor == null) {
            logger.trace("Ticket encryption is not enabled. Falling back to default behavior");
            return result;
        }

        if (result == null) {
            return result;
        }

        logger.info("Attempting to decode [{}]",  result);
        final EncodedTicket encodedTicket = (EncodedTicket) result;

        final Ticket ticket = CompressionUtils.decodeAndSerializeObject(
                encodedTicket.getEncoded(), this.cipherExecutor, Ticket.class);
        logger.info("Decoded [{}]",  ticket);
        return ticket;
    }

    /**
     * Decode tickets.
     *
     * @param items the items
     * @return the set
     */
    protected Collection<Ticket>  decodeTickets(final Collection<Ticket> items) {
        if (this.cipherExecutor == null) {
            logger.trace("Ticket encryption is not enabled. Falling back to default behavior");
            return items;
        }

        if (items == null || items.isEmpty()) {
            return items;
        }

        final Set<Ticket> tickets = new HashSet<>(items.size());
        for (final Ticket item : items) {
            final Ticket ticket = decodeTicket(item);
            tickets.add(ticket);
        }
        return tickets;
    }

}
