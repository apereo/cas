package org.jasig.cas.ticket.registry.support;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.jasig.cas.ticket.Ticket;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TicketRegistryCleanerHelperTests
{
    private TicketRegistryCleanerHelper helper = new TicketRegistryCleanerHelper();

    private TicketRegistry ticketRegistry;

    @Before
    public void setup()
    {
        ticketRegistry = mock(TicketRegistry.class);
    }

    @Test
    public void deleteExpiredTickets_whenZeroExpiredTickets_zeroTicketsAreDeleted()
    {
        int numTicketsRemoved = helper.deleteExpiredTickets(ticketRegistry, Arrays.asList(getUnexpiredTicket("id"), getUnexpiredTicket("id"), getUnexpiredTicket("id")), false);

        verify(ticketRegistry, times(0)).deleteTicket(anyString());
        assertEquals(0, numTicketsRemoved);
    }

    @Test
    public void deleteExpiredTickets_whenSomeTicketsAreExpired_ticketsAreDeleted()
    {
        int numTicketsRemoved = helper.deleteExpiredTickets(ticketRegistry, Arrays.asList(getExpiredTicket("expired1"), getUnexpiredTicket("foo"), getExpiredTicket("expired2")), false);

        verify(ticketRegistry).deleteTicket("expired1");
        verify(ticketRegistry).deleteTicket("expired2");
        assertEquals(2, numTicketsRemoved);
    }

    @Test
    public void deleteExpiredTickets_whenAllTicketsAreExpired_AlTicketsAreDeleted()
    {
        int numTicketsRemoved = helper.deleteExpiredTickets(ticketRegistry, Arrays.asList(getExpiredTicket("expired1"), getExpiredTicket("expired2"), getExpiredTicket("expired3")), false);

        verify(ticketRegistry).deleteTicket("expired1");
        verify(ticketRegistry).deleteTicket("expired2");
        verify(ticketRegistry).deleteTicket("expired3");
        assertEquals(3, numTicketsRemoved);
    }

    @Test
    public void deleteExpiredTickets_whenTicketGrantingTicketIsExpiredAndLogUserOfService_expiresTicket()
    {
        TicketGrantingTicket ticket = mock(TicketGrantingTicket.class);
        when(ticket.isExpired()).thenReturn(true);

        List<Ticket> tickets = new ArrayList<Ticket>();
        tickets.add(ticket);

        helper.deleteExpiredTickets(ticketRegistry, tickets, true);

        verify(ticket).expire();
    }

    @Test
    public void deleteExpiredTickets_whenTicketGrantingTicketIsExpiredAndNotLogUserOfService_doesNotExpireTicket()
    {
        TicketGrantingTicket ticket = mock(TicketGrantingTicket.class);
        when(ticket.isExpired()).thenReturn(true);

        List<Ticket> tickets = new ArrayList<Ticket>();
        tickets.add(ticket);

        helper.deleteExpiredTickets(ticketRegistry, tickets, false);

        verify(ticket, times(0)).expire();
    }

    private Ticket getExpiredTicket(String id)
    {
        Ticket ticket = mock(Ticket.class);
        when(ticket.isExpired()).thenReturn(true);
        when(ticket.getId()).thenReturn(id);
        return ticket;
    }

    private Ticket getUnexpiredTicket(String id)
    {
        Ticket ticket = mock(Ticket.class);
        when(ticket.isExpired()).thenReturn(false);
        when(ticket.getId()).thenReturn(id);
        return ticket;
    }
}
