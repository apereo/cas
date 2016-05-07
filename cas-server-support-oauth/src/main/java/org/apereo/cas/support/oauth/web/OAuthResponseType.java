package org.apereo.cas.support.oauth.web;

/**
 * The OAuth response types (on the authorize request).
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
public enum OAuthResponseType {
    /**
     * For authorization grant type.
     */
    CODE,
    /**
     * For implicit grant type.
     */
    TOKEN
}
