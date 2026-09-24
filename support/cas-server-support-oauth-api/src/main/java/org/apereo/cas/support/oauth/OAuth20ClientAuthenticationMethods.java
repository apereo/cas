package org.apereo.cas.support.oauth;

import module java.base;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.Strings;

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
    TLS_CLIENT_AUTH("tls_client_auth"),

    /**
     * No client authentication at all. Used by public clients that hold no credentials, and by
     * grants that carry their own proof of authorization such as the OpenID4VCI pre-authorized
     * code, where the request is authenticated by the code itself rather than by the client.
     */
    NONE("none");

    private final String type;

    /**
     * Parse oauth20 client authentication methods.
     *
     * @param type the type
     * @return the oauth20 client authentication methods
     */
    public static OAuth20ClientAuthenticationMethods parse(final String type) {
        return Arrays.stream(OAuth20ClientAuthenticationMethods.values())
            .filter(method -> Strings.CI.equals(method.getType(), type))
            .findFirst()
            .orElseThrow();
    }
}
