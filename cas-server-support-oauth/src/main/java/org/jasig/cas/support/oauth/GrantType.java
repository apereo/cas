package org.jasig.cas.support.oauth;

/**
 * Externalizes the grant type strings as specified in RFC-6749.
 *
 * @see http://tools.ietf.org/html/rfc6749
 * @author Joe McCall
 */
public final class GrantType {
    public static final String AUTHORIZATION_CODE = "authorization_code";
    public static final String PASSWORD = "password";
    public static final String CLIENT_CREDENTIALS = "client_credentials";
    public static final String REFRESH_TOKEN = "refresh_token";

    /**
     * Prevents anyone from accidentally instantiating this.
     */
    private GrantType() {};
}
