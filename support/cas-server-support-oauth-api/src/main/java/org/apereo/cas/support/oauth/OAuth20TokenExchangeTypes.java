package org.apereo.cas.support.oauth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.Arrays;

/**
 * The token type identifiers defined for OAuth token exchange.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Getter
@RequiredArgsConstructor
public enum OAuth20TokenExchangeTypes {
    /**
     * The token type identifier for an access token in OAuth token exchange.
     */
    ACCESS_TOKEN("urn:ietf:params:oauth:token-type:access_token"),
    /**
     * In native sso flows, allows the exchange of a device secret for an access token
     * based on the actor token.
     */
    DEVICE_SECRET("urn:openid:params:token-type:device-secret"),
    /**
     * The token type identifier for an ID token in OAuth token exchange.
     */
    ID_TOKEN("urn:ietf:params:oauth:token-type:id_token"),
    /**
     * The token type identifier for a JSON Web Token (JWT) in OAuth token exchange.
     * CAS does not support signed or encrypted JWTs; there is no standard way to
     * obtain the key to verify the signature of a JWT with.
     */
    JWT("urn:ietf:params:oauth:token-type:jwt");

    private final String type;

    /**
     * From token exchange type name to proper type.
     *
     * @param typeName the type name
     * @return  exchange type
     */
    public static OAuth20TokenExchangeTypes from(final String typeName) {
        return Arrays.stream(values()).filter(type -> type.getType().equalsIgnoreCase(typeName)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown token type: " + typeName));
    }
}

