package org.apereo.cas.ticket.factory;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.TransientSessionTicketImpl;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.util.HostNameBasedUniqueTicketIdGenerator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.io.Serializable;
import java.util.Map;

/**
 * This is {@link DefaultTransientSessionTicketFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiredArgsConstructor
@Getter
public class DefaultTransientSessionTicketFactory implements TransientSessionTicketFactory<TransientSessionTicket> {
    private final ExpirationPolicyBuilder<TransientSessionTicket> expirationPolicyBuilder;

    private final UniqueTicketIdGenerator ticketIdGenerator = new HostNameBasedUniqueTicketIdGenerator();

    @Override
    public TransientSessionTicket create(final Service service, final Map<String, Serializable> properties) throws Throwable {
        val id = ticketIdGenerator.getNewTicketId(TransientSessionTicket.PREFIX);
        val expirationPolicy = TransientSessionTicketFactory.buildExpirationPolicy(this.expirationPolicyBuilder, properties);
        return new TransientSessionTicketImpl(id, expirationPolicy, service, properties);
    }

    @Override
    public TransientSessionTicket create(final String id, final Service service, final Map<String, Serializable> properties) {
        val expirationPolicy = TransientSessionTicketFactory.buildExpirationPolicy(this.expirationPolicyBuilder, properties);
        return new TransientSessionTicketImpl(TransientSessionTicketFactory.normalizeTicketId(id),
            expirationPolicy, service, properties);
    }

    @Override
    public Class<? extends Ticket> getTicketType() {
        return TransientSessionTicket.class;
    }
}
