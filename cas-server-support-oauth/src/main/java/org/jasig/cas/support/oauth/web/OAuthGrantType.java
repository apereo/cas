package org.jasig.cas.support.oauth.web;

/**
 * The OAuth grant types (on the access token request).
 *
 * @author Jerome Leleu
 * @since 4.3.0
 */
public enum OAuthGrantType {
    /**
     * For the authorization grant type.
     */
    AUTHORIZATION_CODE,
    /**
     * For the resource owner password grant type.
     */
    PASSWORD,
    /**
     * For the refresh token grant type.
     */
    REFRESH_TOKEN
}
