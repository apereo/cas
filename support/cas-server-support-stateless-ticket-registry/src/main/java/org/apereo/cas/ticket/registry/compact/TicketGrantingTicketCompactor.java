package org.apereo.cas.ticket.registry.compact;

import module java.base;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketGrantingTicketImpl;
import org.apereo.cas.ticket.registry.TicketCompactor;
import org.apereo.cas.ticket.serialization.TicketSerializationManager;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * This is {@link TicketGrantingTicketCompactor}.
 * The ticket is kept as the serialized form of a copy that carries no id, since the id is the compact ticket itself,
 * and none of the maps that track granted tickets, since the stateless registry does not track single sign-on sessions.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class TicketGrantingTicketCompactor implements TicketCompactor<TicketGrantingTicket> {
    private static final int TICKET_INDEX = 2;

    private final TicketSerializationManager ticketSerializationManager;

    @Override
    public void compactFields(final List<String> fields, final Ticket ticket) {
        fields.add(ticketSerializationManager.serializeTicket(toSessionTicket(ticket)));
    }

    @Override
    public Class<TicketGrantingTicket> getTicketType() {
        return TicketGrantingTicket.class;
    }

    @Override
    public Ticket expand(final String compactTicket) {
        val structure = parse(compactTicket, TICKET_INDEX + 1);
        return ticketSerializationManager.deserializeTicket(structure.get(TICKET_INDEX), getTicketType());
    }

    private static Ticket toSessionTicket(final Ticket ticket) {
        if (ticket.getClass() != TicketGrantingTicketImpl.class) {
            return ticket;
        }
        val source = (TicketGrantingTicketImpl) ticket;
        val sessionTicket = new TicketGrantingTicketImpl(TicketGrantingTicket.PREFIX,
            source.getAuthentication(), source.getExpirationPolicy());
        sessionTicket.setCreationTime(source.getCreationTime());
        sessionTicket.setLastTimeUsed(source.getLastTimeUsed());
        sessionTicket.setPreviousTimeUsed(source.getPreviousTimeUsed());
        sessionTicket.setCountOfUses(source.getCountOfUses());
        sessionTicket.setTenantId(source.getTenantId());
        sessionTicket.setProperties(new HashMap<>(source.getProperties()));
        return sessionTicket;
    }
}
