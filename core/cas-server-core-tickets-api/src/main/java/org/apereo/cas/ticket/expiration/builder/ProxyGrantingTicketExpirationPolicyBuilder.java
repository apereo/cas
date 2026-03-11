package org.apereo.cas.ticket.expiration.builder;

import module java.base;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.RequiredArgsConstructor;

/**
 * This is {@link ProxyGrantingTicketExpirationPolicyBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@RequiredArgsConstructor
public class ProxyGrantingTicketExpirationPolicyBuilder
    implements ExpirationPolicyBuilder<ProxyGrantingTicket> {
    @Serial
    private static final long serialVersionUID = -1597980180617072826L;

    private final ExpirationPolicyBuilder<TicketGrantingTicket> ticketGrantingTicketExpirationPolicyBuilder;

    @Override
    public ExpirationPolicy buildTicketExpirationPolicy() {
        return ticketGrantingTicketExpirationPolicyBuilder.buildTicketExpirationPolicy();
    }

}
