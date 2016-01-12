package org.jasig.cas.support.oauth.ticket.accesstoken;

import org.jasig.cas.support.oauth.ticket.code.OAuthCode;

/**
 * An OAuth access token (is like an OAuth code with a different expiration policy).
 *
 * @author Jerome Leleu
 * @since 4.3.0
 */
public interface AccessToken extends OAuthCode {

    String PREFIX = "AT";
}
