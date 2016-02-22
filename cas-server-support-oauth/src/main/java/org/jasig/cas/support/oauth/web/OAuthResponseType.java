package org.jasig.cas.support.oauth.web;

/**
 * The OAuth response types (on the authorize request).
 *
 * @author Jerome Leleu
 * @since 4.3.0
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
