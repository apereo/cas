package org.apereo.cas.util;

import java.util.Collection;

import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.ticket.Ticket;

/**
 * This ticket registry only stores one ticket at the same time and offers the ability to update a ticket.
 * 
 * @author Jerome Leleu
 * @since 4.0.0
 */
public class MockOnlyOneTicketRegistry implements TicketRegistry {

    private Ticket ticket;
    
    @Override
    public void addTicket(final Ticket ticket) {
        this.ticket = ticket;
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) {
        if (this.ticket == null) {
            throw new IllegalArgumentException("No ticket to update");
        }
        addTicket(ticket);
        return ticket;
    }

    @Override
    public <T extends Ticket> T getTicket(final String ticketId, final Class<T> clazz) {
        return (T) this.ticket;
    }

    @Override
    public Ticket getTicket(final String ticketId) {
        return this.ticket;
    }

    @Override
    public int deleteTicket(final String ticketId) {
        this.ticket = null;
        return 1;
    }

    @Override
    public long deleteAll() {
        return deleteTicket(null);
    }

    @Override
    public Collection<Ticket> getTickets() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public long sessionCount() {
        return 1;
    }

    @Override
    public long serviceTicketCount() {
        return 1;
    }
}
