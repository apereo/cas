package org.jasig.cas.ticket;

import org.jasig.cas.authentication.Authentication;

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
    String PREFIX = "COD";

    /**
     * Get the current authentication.
     *
     * @return the current authentication.
     */
    Authentication getAuthentication();
}
