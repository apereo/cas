package org.apereo.cas.ticket;

import org.apereo.cas.authentication.Authentication;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * This is {@link AuthenticationAwareTicket}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface AuthenticationAwareTicket extends Ticket {
    /**
     * Method to retrieve the authentication.
     *
     * @return the authentication
     */
    Authentication getAuthentication();
}
