package org.jasig.cas.support.oauth.ticket.code;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.ticket.ServiceTicket;

/**
 * An OAuth code (is like a service ticket without PGT grant capability).
 *
 * @author Jerome Leleu
 * @since 4.3.0
 */
public interface OAuthCode extends ServiceTicket {

    /**
     *  The prefix for OAuth codes.
     */
    String PREFIX = "OC";

    /**
     * Get the current authentication.
     *
     * @return the current authentication.
     */
    Authentication getAuthentication();
}
