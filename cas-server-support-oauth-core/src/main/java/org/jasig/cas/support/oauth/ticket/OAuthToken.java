package org.jasig.cas.support.oauth.ticket;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.ticket.ServiceTicket;

/**
 * OAuth tokens are mostly like service tickets: they deal with authentication and service.
 *
 * @author Jerome Leleu
 * @since 4.3.0
 */
public interface OAuthToken extends ServiceTicket {

    /**
     * Get the current authentication.
     *
     * @return the current authentication.
     */
    Authentication getAuthentication();
}
