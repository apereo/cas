package org.apereo.cas.ticket.refreshtoken;

import org.apereo.cas.ticket.OAuthToken;

/**
 * A refresh token is an OAuth token which can be used multiple times and has a very long lifetime.
 * It is used to create new access tokens.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
public interface RefreshToken extends OAuthToken {

    /**
     *  The prefix for refresh tokens.
     */
    String PREFIX = "RT";
}
