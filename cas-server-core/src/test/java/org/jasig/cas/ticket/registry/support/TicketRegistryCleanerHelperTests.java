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
package org.jasig.cas.ticket.registry.support;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.jasig.cas.logout.LogoutManager;
import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Ahsan Rabbani
 *
 * @since 4.0
 */
public class TicketRegistryCleanerHelperTests {
    private TicketRegistryCleanerHelper helper = new TicketRegistryCleanerHelper();

    private TicketRegistry ticketRegistry;
    private LogoutManager logoutManager;

    @Before
    public void setup() {
        ticketRegistry = mock(TicketRegistry.class);
        logoutManager = mock(LogoutManager.class);
    }

    @Test
    public void deleteExpiredTickets_whenZeroExpiredTickets_zeroTicketsAreDeleted() {
        int numTicketsRemoved = helper.deleteExpiredTickets(
                ticketRegistry,
                logoutManager,
                Arrays.asList(getUnexpiredTicket("id"),getUnexpiredTicket("id"), getUnexpiredTicket("id")),
                false
        );

        verify(ticketRegistry, times(0)).deleteTicket(anyString());
        assertEquals(0, numTicketsRemoved);
    }

    @Test
    public void deleteExpiredTickets_whenSomeTicketsAreExpired_ticketsAreDeleted() {
        int numTicketsRemoved = helper.deleteExpiredTickets(
                ticketRegistry,
                logoutManager,
                Arrays.asList(getExpiredTicket("expired1"), getUnexpiredTicket("foo"), getExpiredTicket("expired2")),
                false
        );

        verify(ticketRegistry).deleteTicket("expired1");
        verify(ticketRegistry).deleteTicket("expired2");
        assertEquals(2, numTicketsRemoved);
    }

    @Test
    public void deleteExpiredTickets_whenAllTicketsAreExpired_AlTicketsAreDeleted() {
        int numTicketsRemoved = helper.deleteExpiredTickets(
                ticketRegistry,
                logoutManager,
                Arrays.asList(getExpiredTicket("expired1"), getExpiredTicket("expired2"), getExpiredTicket("expired3")),
                false
        );

        verify(ticketRegistry).deleteTicket("expired1");
        verify(ticketRegistry).deleteTicket("expired2");
        verify(ticketRegistry).deleteTicket("expired3");
        assertEquals(3, numTicketsRemoved);
    }

    @Test
    public void deleteExpiredTickets_whenTicketGrantingTicketIsExpiredAndLogUserOfService_expiresTicket() {
        TicketGrantingTicket ticket = mock(TicketGrantingTicket.class);
        when(ticket.isExpired()).thenReturn(true);

        List<Ticket> tickets = new ArrayList<Ticket>();
        tickets.add(ticket);

        helper.deleteExpiredTickets(ticketRegistry, logoutManager, tickets, true);

        verify(logoutManager).performLogout(ticket);
    }

    @Test
    public void deleteExpiredTickets_whenTicketGrantingTicketIsExpiredAndNotLogUserOfService_doesNotExpireTicket() {
        TicketGrantingTicket ticket = mock(TicketGrantingTicket.class);
        when(ticket.isExpired()).thenReturn(true);

        List<Ticket> tickets = new ArrayList<Ticket>();
        tickets.add(ticket);

        helper.deleteExpiredTickets(ticketRegistry, logoutManager, tickets, false);

        verify(logoutManager, times(0)).performLogout(ticket);
    }

    private Ticket getExpiredTicket(String id) {
        Ticket ticket = mock(Ticket.class);
        when(ticket.isExpired()).thenReturn(true);
        when(ticket.getId()).thenReturn(id);
        return ticket;
    }

    private Ticket getUnexpiredTicket(String id) {
        Ticket ticket = mock(Ticket.class);
        when(ticket.isExpired()).thenReturn(false);
        when(ticket.getId()).thenReturn(id);
        return ticket;
    }
}
