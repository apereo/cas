package org.apereo.cas.uma.ticket;

import org.apereo.cas.ticket.TicketFactory;
import org.apereo.cas.uma.ticket.resource.ResourceSet;

import java.util.Collection;

/**
 * This is {@link UmaPermissionTicketFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public interface UmaPermissionTicketFactory extends TicketFactory {
    /**
     * Create permission ticket uma permission ticket.
     *
     * @param resourceSet the resource set
     * @param scopes      the scopes
     * @return the uma permission ticket
     */
    UmaPermissionTicket create(ResourceSet resourceSet, Collection<String> scopes);
}
