package org.jasig.cas.support.oauth.ticket.code;

import org.jasig.cas.support.oauth.ticket.OAuthToken;

/**
 * An OAuth code is an OAuth token which can be used only once and has a short lifetime.
 * It is used to grant access tokens.
 *
 * @author Jerome Leleu
 * @since 4.3.0
 */
public interface OAuthCode extends OAuthToken {

    /**
     *  The prefix for OAuth codes.
     */
    String PREFIX = "OC";
}
