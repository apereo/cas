package org.apereo.cas.mfa.simple.ticket;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;

import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.Serializable;
import java.util.Map;

/**
 * This is {@link DefaultCasSimpleMultifactorAuthenticationTicketFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
public class DefaultCasSimpleMultifactorAuthenticationTicketFactory implements CasSimpleMultifactorAuthenticationTicketFactory {

    private final ExpirationPolicyBuilder expirationPolicyBuilder;

    private final UniqueTicketIdGenerator ticketIdGenerator;

    @Override
    public CasSimpleMultifactorAuthenticationTicket create(final Service service, final Map<String, Serializable> properties) {
        val id = ticketIdGenerator.getNewTicketId(CasSimpleMultifactorAuthenticationTicket.PREFIX);
        val expirationPolicy = CasSimpleMultifactorAuthenticationTicketFactory.buildExpirationPolicy(this.expirationPolicyBuilder, properties);
        return new CasSimpleMultifactorAuthenticationTicketImpl(id, expirationPolicy, service, properties);
    }

    @Override
    public Class<? extends Ticket> getTicketType() {
        return CasSimpleMultifactorAuthenticationTicket.class;
    }
}
