package org.apereo.cas;

import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.registry.DefaultTicketRegistry;
import org.apereo.cas.util.serialization.SerializationUtils;

import lombok.val;

/**
 * Simulate a remote ticket registry.
 *
 * @author Jerome LELEU
 * @since 6.6.0
 */
public class SimulateRemoteTicketRegistry extends DefaultTicketRegistry {

    @Override
    public void addTicket(final Ticket ticket) throws Exception {
        super.addTicket(simulateTransport(ticket));
    }

    @Override
    public Ticket updateTicket(final Ticket ticket) throws Exception {
        return super.updateTicket(simulateTransport(ticket));
    }

    private Ticket simulateTransport(final Ticket ticket) {
        val data = SerializationUtils.serialize(ticket);
        return SerializationUtils.deserialize(data, ticket.getClass());
    }
}
