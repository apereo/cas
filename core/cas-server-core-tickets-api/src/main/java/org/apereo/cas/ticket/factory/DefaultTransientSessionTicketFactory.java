package org.apereo.cas.ticket.factory;

import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.TransientSessionTicketImpl;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;

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
public class DefaultTransientSessionTicketFactory implements TransientSessionTicketFactory {
    private final ExpirationPolicyBuilder<TransientSessionTicket> expirationPolicyBuilder;

    private final UniqueTicketIdGenerator ticketIdGenerator = new DefaultUniqueTicketIdGenerator();

    /**
     * Create delegated authentication request ticket.
     *
     * @param service    the service
     * @param properties the properties
     * @return the delegated authentication request ticket
     */
    @Override
    public TransientSessionTicket create(final Service service, final Map<String, Serializable> properties) {
        val id = ticketIdGenerator.getNewTicketId(TransientSessionTicket.PREFIX);
        val expirationPolicy = TransientSessionTicketFactory.buildExpirationPolicy(this.expirationPolicyBuilder, properties);
        return new TransientSessionTicketImpl(id, expirationPolicy, service, properties);
    }

    @Override
    public TransientSessionTicket create(final String id, final Map<String, Serializable> properties) {
        val expirationPolicy = TransientSessionTicketFactory.buildExpirationPolicy(this.expirationPolicyBuilder, properties);
        return new TransientSessionTicketImpl(TransientSessionTicketFactory.normalizeTicketId(id),
            expirationPolicy, null, properties);
    }

    @Override
    public TicketFactory get(final Class<? extends Ticket> clazz) {
        return this;
    }
}
