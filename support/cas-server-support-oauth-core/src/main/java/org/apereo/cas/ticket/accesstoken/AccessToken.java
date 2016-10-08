package org.apereo.cas.ticket.accesstoken;

import org.apereo.cas.ticket.OAuthToken;

/**
 * An access token is an OAuth token which can be used multiple times and has a long lifetime.
 * It is used to access resources on behalf of a user and OAuth client.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
public interface AccessToken extends OAuthToken {

    /**
     *  The prefix for access tokens.
     */
    String PREFIX = "AT";
}
