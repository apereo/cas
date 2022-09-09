package org.apereo.cas.ticket;

import org.apereo.cas.util.EncodingUtils;

import lombok.val;

/**
 * This is {@link DefaultSecurityTokenTicketFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public record DefaultSecurityTokenTicketFactory(UniqueTicketIdGenerator ticketUniqueTicketIdGenerator, ExpirationPolicyBuilder expirationPolicy) implements SecurityTokenTicketFactory {

    @Override
    public SecurityTokenTicket create(final TicketGrantingTicket ticket,
                                      final byte[] securityTokenSerialized) {
        val token = EncodingUtils.encodeBase64(securityTokenSerialized);
        val id = ticketUniqueTicketIdGenerator.getNewTicketId(SecurityTokenTicket.PREFIX);
        val stt = new DefaultSecurityTokenTicket(id, ticket, this.expirationPolicy.buildTicketExpirationPolicy(), token);
        ticket.getDescendantTickets().add(stt.getId());
        return stt;
    }

    @Override
    public Class<? extends Ticket> getTicketType() {
        return SecurityTokenTicket.class;
    }
}
