package org.apereo.cas.ticket.expiration.builder;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.ticket.ExpirationPolicy;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.proxy.ProxyGrantingTicket;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * This is {@link ProxyGrantingTicketExpirationPolicyBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiredArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@ToString
@Getter
public class ProxyGrantingTicketExpirationPolicyBuilder implements ExpirationPolicyBuilder<ProxyGrantingTicket> {
    private static final long serialVersionUID = -1597980180617072826L;

    /**
     * The Ticket granting ticket expiration policy builder.
     */
    protected final ExpirationPolicyBuilder<TicketGrantingTicket> ticketGrantingTicketExpirationPolicyBuilder;
    /**
     * The Cas properties.
     */
    protected final CasConfigurationProperties casProperties;

    @Override
    public ExpirationPolicy buildTicketExpirationPolicy() {
        return ticketGrantingTicketExpirationPolicyBuilder.buildTicketExpirationPolicy();
    }

    @Override
    public Class<ProxyGrantingTicket> getTicketType() {
        return ProxyGrantingTicket.class;
    }
}
