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
package org.jasig.cas;

import java.util.Collection;

import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.registry.TicketRegistry;

/**
 * This ticket registry only stores one ticket at the same time and offers the ability to update a ticket.
 * 
 * @author Jerome Leleu
 * @since 4.0.0
 */
public final class MockOnlyOneTicketRegistry implements TicketRegistry {

    private Ticket ticket;
    
    @Override
    public void addTicket(final Ticket ticket) {
        this.ticket = ticket;
    }

    public void updateTicket(final Ticket ticket) {
        // ticket must exist
        if (this.ticket == null) {
            throw new IllegalArgumentException("No ticket to update");
        }
        addTicket(ticket);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Ticket> T getTicket(final String ticketId, final Class<? extends Ticket> clazz) {
        return (T) this.ticket;
    }

    @Override
    public Ticket getTicket(final String ticketId) {
        return this.ticket;
    }

    @Override
    public boolean deleteTicket(final String ticketId) {
        this.ticket = null;
        return false;
    }

    @Override
    public Collection<Ticket> getTickets() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
