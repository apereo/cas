package org.apereo.cas.logout;

import module java.base;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.util.NamedObject;
import org.springframework.core.Ordered;

/**
 * This is {@link LogoutPostProcessor} that knows how to carry out specific operations upon logout events.
 * Example ops may include removing tickets from the registry, deleting cookies, sending events, etc.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@FunctionalInterface
public interface LogoutPostProcessor extends Ordered, NamedObject {

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
}
