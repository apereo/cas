package org.apereo.cas.ticket;

import org.apereo.cas.authentication.Authentication;

/**
 * This is {@link IdentifiableTicket}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
public interface IdentifiableTicket extends Ticket {
    /**
     * Method to retrieve the authentication.
     *
     * @return the authentication
     */
    Authentication getAuthentication();
}
