package org.apereo.cas.ticket;

import org.apereo.cas.util.EncodingUtils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

/**
 * This is {@link DefaultSecurityTokenTicketFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredArgsConstructor
public class DefaultSecurityTokenTicketFactory implements SecurityTokenTicketFactory {

    @Getter
    private final UniqueTicketIdGenerator ticketIdGenerator;
    
    @Getter
    private final ExpirationPolicyBuilder expirationPolicyBuilder;

    @Override
    public SecurityTokenTicket create(final TicketGrantingTicket ticket,
                                      final byte[] securityTokenSerialized) throws Throwable {
        val token = EncodingUtils.encodeBase64(securityTokenSerialized);
        val id = ticketIdGenerator.getNewTicketId(SecurityTokenTicket.PREFIX);
        val stt = new DefaultSecurityTokenTicket(id, ticket, this.expirationPolicyBuilder.buildTicketExpirationPolicy(), token);
        ticket.getDescendantTickets().add(stt.getId());
        return stt;
    }

    @Override
    public Class<? extends Ticket> getTicketType() {
        return SecurityTokenTicket.class;
    }
}
