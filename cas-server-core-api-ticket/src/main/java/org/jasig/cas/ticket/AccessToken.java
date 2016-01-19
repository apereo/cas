package org.jasig.cas.ticket;

/**
 * An OAuth access token (is like an OAuth code with a different expiration policy).
 *
 * @author Jerome Leleu
 * @since 4.3.0
 */
public interface AccessToken extends OAuthCode {

    /**
     *  The prefix for access tokens.
     */
    String PREFIX = "AT";
}
