package org.apereo.cas.uma.ticket;

import org.apereo.cas.ticket.TicketFactory;

/**
 * This is {@link UmaPermissionTicketFactory}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
public interface UmaPermissionTicketFactory extends TicketFactory {
    UmaPermissionTicket createPermissionTicket();
}
