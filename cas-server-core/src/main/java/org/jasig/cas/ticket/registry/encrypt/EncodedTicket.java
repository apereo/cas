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


import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;

/**
 * Ticket implementation that encodes a source ticket and stores the encoded
 * representation internally.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
public final class EncodedTicket implements Ticket {

    private static final long serialVersionUID = -7078771807487764116L;
    private String id;

    private byte[] encodedTicket;

    /** Private ctor used for serialization only. **/
    private EncodedTicket() {}

    /**
     * Creates a new encoded ticket using the given encoder to encode the given
     * source ticket.
     *
     * @param encodedTicket the encoded ticket
     * @param encodedTicketId the encoded ticket id
     */
    public EncodedTicket(final byte[] encodedTicket, final String encodedTicketId) {
        this.id = encodedTicketId;
        this.encodedTicket = encodedTicket;
    }

    @Override
    public int getCountOfUses() {
        throw new UnsupportedOperationException("getCountOfUses() operation not supported");
    }

    @Override
    public long getCreationTime() {
        throw new UnsupportedOperationException("getCreationTime() operation not supported");
    }

    @Override
    public TicketGrantingTicket getGrantingTicket() {
        throw new UnsupportedOperationException("getGrantingTicket() operation not supported");
    }

    /**
     * Gets an encoded version of ID of the source ticket.
     *
     * @return Encoded ticket ID.
     */
    @Override
    public String getId() {
        return this.id;
    }

    public byte[] getEncoded() {
        return this.encodedTicket;
    }

    @Override
    public boolean isExpired() {
        throw new UnsupportedOperationException("isExpired() operation not supported");
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.NO_CLASS_NAME_STYLE)
                .append(this.id).build();
    }
}
