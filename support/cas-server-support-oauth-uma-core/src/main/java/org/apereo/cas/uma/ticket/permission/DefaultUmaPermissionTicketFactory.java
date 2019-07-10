package org.apereo.cas.uma.ticket.permission;

import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.uma.ticket.resource.ResourceSet;
import org.apereo.cas.util.DefaultUniqueTicketIdGenerator;

import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Collection;
import java.util.Map;

/**
 * This is {@link DefaultUmaPermissionTicketFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
public class DefaultUmaPermissionTicketFactory implements UmaPermissionTicketFactory {
    /**
     * Default instance for the ticket id generator.
     */
    protected final UniqueTicketIdGenerator idGenerator;

    /**
     * ExpirationPolicy for refresh tokens.
     */
    protected final ExpirationPolicyBuilder expirationPolicy;

    public DefaultUmaPermissionTicketFactory(final ExpirationPolicyBuilder expirationPolicy) {
        this(new DefaultUniqueTicketIdGenerator(), expirationPolicy);
    }

    @Override
    public UmaPermissionTicket create(final ResourceSet resourceSet, final Collection<String> scopes, final Map<String, Object> claims) {
        val codeId = this.idGenerator.getNewTicketId(UmaPermissionTicket.PREFIX);
        return new DefaultUmaPermissionTicket(codeId, resourceSet, this.expirationPolicy.buildTicketExpirationPolicy(), scopes, claims);
    }

    @Override
    public TicketFactory get(final Class<? extends Ticket> clazz) {
        return this;
    }


}
