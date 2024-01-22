package org.apereo.cas.ticket.registry.compact;

import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.registry.TicketCompactor;

/**
 * This is {@link BaseTicketCompactor}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
public abstract class BaseTicketCompactor<T extends Ticket> implements TicketCompactor<T> {
}
