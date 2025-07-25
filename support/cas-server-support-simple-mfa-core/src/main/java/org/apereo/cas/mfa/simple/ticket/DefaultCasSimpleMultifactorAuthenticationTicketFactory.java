package org.apereo.cas.mfa.simple.ticket;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.Strings;
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

    @Getter
    private final ExpirationPolicyBuilder expirationPolicyBuilder;

    private final UniqueTicketIdGenerator ticketIdGenerator;

    @Override
    public CasSimpleMultifactorAuthenticationTicket create(final Service service,
                                                           final Map<String, Serializable> properties) throws Throwable {
        val id = ticketIdGenerator.getNewTicketId(CasSimpleMultifactorAuthenticationTicket.PREFIX);
        return create(id, service, properties);
    }

    @Override
    public CasSimpleMultifactorAuthenticationTicket create(final String id, final Service service, final Map<String, Serializable> properties) {
        val expirationPolicy = CasSimpleMultifactorAuthenticationTicketFactory.buildExpirationPolicy(this.expirationPolicyBuilder, properties);
        return new CasSimpleMultifactorAuthenticationTicketImpl(Strings.CI.remove(id, "\""), expirationPolicy, service, properties);
    }

    @Override
    public Class<? extends Ticket> getTicketType() {
        return CasSimpleMultifactorAuthenticationTicket.class;
    }
}
