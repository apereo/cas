package org.apereo.cas.ticket.code;

import org.apereo.cas.ticket.OAuthToken;

/**
 * An OAuth code is an OAuth token which can be used only once and has a short lifetime.
 * It is used to grant access tokens.
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
public interface OAuthCode extends OAuthToken {

    /**
     *  The prefix for OAuth codes.
     */
    String PREFIX = "OC";
}
