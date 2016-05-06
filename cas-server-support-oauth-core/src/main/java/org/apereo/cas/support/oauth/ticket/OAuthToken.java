package org.apereo.cas.support.oauth.ticket;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.ticket.ServiceTicket;

/**
 * OAuth tokens are mostly like service tickets: they deal with authentication and service.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
public interface OAuthToken extends ServiceTicket {

    /**
     * Get the current authentication.
     *
     * @return the current authentication.
     */
    Authentication getAuthentication();
}
