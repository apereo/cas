package org.apereo.cas.ticket;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.cxf.ws.security.tokenstore.SecurityToken;
import org.apereo.cas.util.EncodingUtils;

/**
 * This is {@link DefaultSecurityTokenTicketFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class DefaultSecurityTokenTicketFactory implements SecurityTokenTicketFactory {

    private final ExpirationPolicy expirationPolicy;
    private final UniqueTicketIdGenerator ticketUniqueTicketIdGenerator;

    public DefaultSecurityTokenTicketFactory(final UniqueTicketIdGenerator ticketGrantingTicketUniqueTicketIdGenerator,
                                             final ExpirationPolicy expirationPolicy) {
        this.expirationPolicy = expirationPolicy;
        this.ticketUniqueTicketIdGenerator = ticketGrantingTicketUniqueTicketIdGenerator;
    }

    @Override
    public <T extends TicketFactory> T get(final Class<? extends Ticket> clazz) {
        return (T) this;
    }

    @Override
    public SecurityTokenTicket create(final TicketGrantingTicket ticket, final SecurityToken securityToken) {
        final String token = EncodingUtils.encodeBase64(SerializationUtils.serialize(securityToken));
        final String id = ticketUniqueTicketIdGenerator.getNewTicketId(SecurityTokenTicket.PREFIX);
        final SecurityTokenTicket stt = new DefaultSecurityTokenTicket(id, ticket, this.expirationPolicy, token);
        ticket.getDescendantTickets().add(stt.getId());
        return stt;
    }
}
