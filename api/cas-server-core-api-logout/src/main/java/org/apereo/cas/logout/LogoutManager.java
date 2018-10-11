package org.apereo.cas.logout;

import org.apereo.cas.logout.slo.SingleLogoutRequest;
import org.apereo.cas.ticket.TicketGrantingTicket;

import java.util.List;

/**
 * A logout manager handles the Single Log Out process.
 *
 * @author Jerome Leleu
 * @since 4.0.0
 */
@FunctionalInterface
public interface LogoutManager {

    /**
     * Perform a back channel logout for a given ticket granting ticket and returns all the logout requests.
     *
     * @param ticket a given ticket granting ticket.
     * @return all logout requests.
     */
    List<SingleLogoutRequest> performLogout(TicketGrantingTicket ticket);
}
