package org.apereo.cas.ticket.accesstoken;

import org.apereo.cas.ticket.OAuth20Token;

/**
 * An access token is an OAuth token which can be used multiple times and has a long lifetime.
 * It is used to access resources on behalf of a user and OAuth client.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
public interface OAuth20AccessToken extends OAuth20Token {

    /**
     * The prefix for access tokens.
     */
    String PREFIX = "AT";

    /**
     * Sets id token.
     *
     * @param idToken the id token
     */
    void setIdToken(String idToken);

    /**
     * Gets id token.
     *
     * @return the id token
     */
    String getIdToken();

    /**
     * Gets token or code that is exchanged
     * for this access token.
     *
     * @return the token
     */
    String getToken();

    /**
     * Expiration time of the Access Token in seconds since the response was generated.
     * @return access token expiration in seconds
     */
    long getExpiresIn();
}
