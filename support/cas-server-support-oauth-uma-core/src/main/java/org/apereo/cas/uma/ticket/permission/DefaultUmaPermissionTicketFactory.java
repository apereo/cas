package org.apereo.cas.uma.ticket.permission;

import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.uma.ticket.resource.ResourceSet;

import lombok.Getter;
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
@Getter
public class DefaultUmaPermissionTicketFactory implements UmaPermissionTicketFactory {
    @Getter
    protected final UniqueTicketIdGenerator ticketIdGenerator;
    protected final ExpirationPolicyBuilder expirationPolicyBuilder;

    @Override
    public UmaPermissionTicket create(final ResourceSet resourceSet, final Collection<String> scopes,
                                      final Map<String, Object> claims) throws Throwable {
        val codeId = ticketIdGenerator.getNewTicketId(UmaPermissionTicket.PREFIX);
        return new DefaultUmaPermissionTicket(codeId, resourceSet, expirationPolicyBuilder.buildTicketExpirationPolicy(), scopes, claims);
    }

    @Override
    public Class<? extends Ticket> getTicketType() {
        return UmaPermissionTicket.class;
    }

}
