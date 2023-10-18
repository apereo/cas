package org.apereo.cas.support.oauth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The OAuth client authentication methods.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Getter
@RequiredArgsConstructor
public enum OAuth20ClientAuthenticationMethods {

    /**
     * Authentication method for basic authn.
     */
    CLIENT_SECRET_BASIC("client_secret_basic"),

    /**
     * Authentication method for form-post authn.
     */
    CLIENT_SECRET_POST("client_secret_post"),

    /**
     * Authentication method using HMAC and JWT.
     */
    CLIENT_SECRET_JWT("client_secret_jwt"),

    /**
     * Authentication method using private key JWT.
     */
    PRIVATE_KEY_JWT("private_key_jwt"),

    /**
     * Authentication method using mTLS.
     */
    TLS_CLIENT_AUTH("tls_client_auth");

    private final String type;
}
