package org.apereo.cas.logout;

import org.apereo.cas.ticket.TicketGrantingTicket;
import org.springframework.core.Ordered;

/**
 * This is {@link LogoutHandler} that knows how to carry out specific operations upon logout events.
 * Example ops may include removing tickets from the registry, deleting cookies, sending events, etc.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@FunctionalInterface
public interface LogoutHandler extends Ordered {

    @Override
    default int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    /**
     * Handle.
     *
     * @param ticketGrantingTicket the ticket granting ticket
     */
    void handle(TicketGrantingTicket ticketGrantingTicket);

    /**
     * Gets name.
     *
     * @return the name
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }
}
