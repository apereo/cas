package org.apereo.cas.ticket.registry.compact;

import module java.base;
import org.apereo.cas.ticket.AbstractTicket;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.registry.TicketCompactor;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import org.apereo.cas.util.DigestUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * This is {@link TicketGrantingTicketCompactor}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class TicketGrantingTicketCompactor implements TicketCompactor<TicketGrantingTicket> {
    private final TicketSerializationManager ticketSerializationManager;

    @Override
    public String compact(final Ticket ticket) {
        if (ticket.isStateless() && ticket instanceof final AbstractTicket expandedTicket) {
            val encodedId = expandedTicket.getId();
            expandedTicket.setId(ticket.getPrefix() + UniqueTicketIdGenerator.SEPARATOR + DigestUtils.sha256(encodedId));
            try {
                return ticketSerializationManager.serializeTicket(ticket);
            } finally {
                expandedTicket.setId(encodedId);
            }
        }
        return ticketSerializationManager.serializeTicket(ticket);
    }

    @Override
    public Class<TicketGrantingTicket> getTicketType() {
        return TicketGrantingTicket.class;
    }

    @Override
    public Ticket expand(final String ticketId) {
        return ticketSerializationManager.deserializeTicket(ticketId, getTicketType());
    }
}
