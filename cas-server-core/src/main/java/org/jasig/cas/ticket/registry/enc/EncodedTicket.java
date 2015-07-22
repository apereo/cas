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

package org.jasig.cas.ticket.registry.enc;


import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;

/**
 * Ticket implementation that encodes a source ticket and stores the encoded
 * representation internally.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public final class EncodedTicket implements Ticket {
    private static final long serialVersionUID = 1L;

    private final String id;

    private final Ticket source;

    private final String encodedTicket;

    /**
     * Creates a new encoded ticket using the given encoder to encode the given
     * source ticket.
     *
     * @param source Source ticket.
     * @param encodedTicket the encoded ticket
     * @param encodedTicketId the encoded ticket id
     */
    public EncodedTicket(final Ticket source, final String encodedTicket, final String encodedTicketId) {
        this.id = encodedTicketId;
        this.encodedTicket = encodedTicket;
        this.source = source;
    }

    @Override
    public int getCountOfUses() {
        return source.getCountOfUses();
    }

    @Override
    public long getCreationTime() {
        return source.getCreationTime();
    }

    @Override
    public TicketGrantingTicket getGrantingTicket() {
        return source.getGrantingTicket();
    }

    /**
     * Gets an encoded version of ID of the source ticket.
     *
     * @return Encoded ticket ID.
     *
     * @see org.jasig.cas.ticket.Ticket#getId()
     */
    @Override
    public String getId() {
        return this.id;
    }

    public String getEncoded() {
        return this.encodedTicket;
    }

    @Override
    public boolean isExpired() {
        return source.isExpired();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append(this.id).build();
    }
}
