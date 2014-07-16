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

package org.jasig.cas.ticket.enc;

import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;

/**
 * Ticket implementation that encodes a source ticket and stores the encoded
 * representation internally such that the original ticket can be produced by
 * calling the {@link #decode()} method.
 *
 * @author Marvin S. Addison
 * @author Misagh Moayyed
 * @since 4.1
 */
public final class EncodedTicket implements Ticket {
    private static final long serialVersionUID = 1L;

    private final String id;

    private final ReversibleEncoder encoder;

    private final String encodedTicket;

    /**
     * Creates a new encoded ticket using the given encoder to encode the given
     * source ticket.
     *
     * @param encoder Ticket encoder.
     * @param source Source ticket.
     */
    public EncodedTicket(final ReversibleEncoder encoder, final Ticket source) {
        this.encoder = encoder;
        this.id = encodeId(encoder, source.getId());
        this.encodedTicket = encoder.encode(source);
    }

    /**
     * Produces the original ticket by decoding the internal encoded ticket
     * string.
     *
     * @return Source ticket.
     */
    public Ticket decode() {
        return (Ticket) this.encoder.decode(this.encodedTicket);
    }

    /**
     * @return Returns 0.
     * @see org.jasig.cas.ticket.Ticket#getCountOfUses()
     */
    public int getCountOfUses() {
        return 0;
    }

    /**
     * @return Returns 0.
     * @see org.jasig.cas.ticket.Ticket#getCreationTime()
     */
    public long getCreationTime() {
        return 0;
    }

    /**
     * @return Returns null.
     * @see org.jasig.cas.ticket.Ticket#getGrantingTicket()
     */
    public TicketGrantingTicket getGrantingTicket() {
        return null;
    }

    /**
     * Gets an encoded version of ID of the source ticket.
     *
     * @return Encoded ticket ID.
     *
     * @see org.jasig.cas.ticket.Ticket#getId()
     */
    public String getId() {
        return this.id;
    }

    /**
     * @return Returns false.
     * @see org.jasig.cas.ticket.Ticket#isExpired()
     */
    public boolean isExpired() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "EncodedTicket: " + this.id;
    }


    /**
     * Encode id.
     *
     * @param encoder the encoder
     * @param ticketId the ticket id
     * @return the encoded string
     */
    public static String encodeId(final ReversibleEncoder encoder, final String ticketId) {
        return encoder.encode(ticketId.getBytes());
    }
}
