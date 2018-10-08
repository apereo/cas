package org.apereo.cas.logout;

import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.ticket.TicketGrantingTicket;

import java.util.Collection;

/**
 * This is {@link SingleLogoutServiceMessageHandler} which defines how a logout message
 * for a service that supports SLO should be handled.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@FunctionalInterface
public interface SingleLogoutServiceMessageHandler {

    /**
     * Handle logout for slo service.
     *
     * @param singleLogoutService  the service
     * @param ticketId             the ticket id
     * @param ticketGrantingTicket the ticket granting ticket
     * @return the logout request
     */
    Collection<LogoutRequest> handle(WebApplicationService singleLogoutService, String ticketId, TicketGrantingTicket ticketGrantingTicket);

    /**
     * Gets name.
     *
     * @return the name
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Supports handling the logout message.
     *
     * @param service the service
     * @return the boolean
     */
    default boolean supports(WebApplicationService service) {
        return true;
    }
}
