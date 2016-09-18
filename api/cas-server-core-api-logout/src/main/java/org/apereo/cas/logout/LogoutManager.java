package org.apereo.cas.logout;

import java.util.List;

import org.apereo.cas.ticket.TicketGrantingTicket;

/**
 * A logout manager handles the Single Log Out process.
 *
 * @author Jerome Leleu
 * @since 4.0.0
 */
public interface LogoutManager {

    /**
     * Perform a back channel logout for a given ticket granting ticket and returns all the logout requests.
     *
     * @param ticket a given ticket granting ticket.
     * @return all logout requests.
     */
    List<LogoutRequest> performLogout(TicketGrantingTicket ticket);

    /**
     * Create a logout message for front channel logout.
     *
     * @param logoutRequest the logout request.
     * @return a front SAML logout message.
     */
    String createFrontChannelLogoutMessage(LogoutRequest logoutRequest);
}
