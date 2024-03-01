package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationResult;
import org.apereo.cas.ticket.Ticket;

/**
 * This is {@link SingleSignOnBuildingStrategy}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@FunctionalInterface
public interface SingleSignOnBuildingStrategy {
    /**
     * Default implementation bean name.
     */
    String BEAN_NAME = "singleSignOnBuildingStrategy";

    /**
     * Build ticket granting ticket.
     *
     * @param authenticationResult the authentication result
     * @param authentication       the authentication
     * @param ticketGrantingTicket the ticket granting ticket
     * @return the ticket
     */
    Ticket buildTicketGrantingTicket(AuthenticationResult authenticationResult,
                                     Authentication authentication,
                                     String ticketGrantingTicket);
}
