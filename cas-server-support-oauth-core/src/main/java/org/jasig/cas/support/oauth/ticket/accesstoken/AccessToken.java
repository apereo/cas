package org.jasig.cas.support.oauth.ticket.accesstoken;

import org.jasig.cas.support.oauth.ticket.OAuthToken;

/**
 * An access token is an OAuth token which can be used multiple times and has a long lifetime.
 * It is used to access resources on behalf of a user and OAuth client.
 *
 * @author Jerome Leleu
 * @since 4.3.0
 */
public interface AccessToken extends OAuthToken {

    /**
     *  The prefix for access tokens.
     */
    String PREFIX = "AT";
}
