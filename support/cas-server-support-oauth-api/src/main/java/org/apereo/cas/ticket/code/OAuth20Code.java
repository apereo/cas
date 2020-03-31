package org.apereo.cas.ticket.code;

import org.apereo.cas.ticket.OAuth20Token;

/**
 * An OAuth code is an OAuth token which can be used only once and has a short lifetime.
 * It is used to grant access tokens.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
public interface OAuth20Code extends OAuth20Token {

    /**
     * The prefix for OAuth codes.
     */
    String PREFIX = "OC";

    /**
     * The PKCE code challenge.
     *
     * @return code challenge
     */
    String getCodeChallenge();

    /**
     * The PKCE code challenge method.
     *
     * @return code challenge method (i.e. plain, S256, etc)
     */
    String getCodeChallengeMethod();
}
