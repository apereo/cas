package org.apereo.cas.ticket.accesstoken;

import org.apereo.cas.ticket.OAuth20Token;
import org.apereo.cas.ticket.ServiceAwareTicket;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * An access token is an OAuth token which can be used multiple times and has a long lifetime.
 * It is used to access resources on behalf of a user and OAuth client.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public interface OAuth20AccessToken extends OAuth20Token, ServiceAwareTicket {

    /**
     * The prefix for access tokens.
     */
    String PREFIX = "AT";

    /**
     * Sets ID token.
     *
     * @param idToken the ID token
     */
    void setIdToken(String idToken);

    /**
     * Gets ID token.
     *
     * @return the ID token
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
