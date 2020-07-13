package org.apereo.cas.logout.slo;

import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.ticket.TicketGrantingTicket;

import org.springframework.core.Ordered;

import java.util.Collection;

/**
 * This is {@link SingleLogoutServiceMessageHandler} which defines how a logout message
 * for a service that supports SLO should be handled.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface SingleLogoutServiceMessageHandler extends Ordered {

    /**
     * Handle logout for slo service.
     *
     * @param singleLogoutService  the service
     * @param ticketId             the ticket id
     * @param ticketGrantingTicket the ticket granting ticket
     * @return the logout request
     */
    Collection<SingleLogoutRequest> handle(WebApplicationService singleLogoutService, String ticketId, TicketGrantingTicket ticketGrantingTicket);

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
     * @return true/false
     */
    default boolean supports(final WebApplicationService service) {
        return service != null;
    }

    /**
     * Log out of a service through back channel.
     *
     * @param request the logout request.
     * @return if the logout has been performed.
     */
    boolean performBackChannelLogout(SingleLogoutRequest request);

    /**
     * Create a logout message typically for front channel logout.
     *
     * @param logoutRequest the logout request.
     * @return the single logout message payload
     */
    SingleLogoutMessage createSingleLogoutMessage(SingleLogoutRequest logoutRequest);

    @Override
    default int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
