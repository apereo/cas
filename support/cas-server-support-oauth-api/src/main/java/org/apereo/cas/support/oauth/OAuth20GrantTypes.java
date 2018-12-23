package org.apereo.cas.support.oauth;

import lombok.Getter;

/**
 * The OAuth grant types (on the access token request).
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
@Getter
public enum OAuth20GrantTypes {

    /**
     * Indicates the absence of invalidity of a grant type.
     */
    NONE("none"),
    /**
     * For the authorization grant type.
     */
    AUTHORIZATION_CODE("authorization_code"),
    /**
     * For the resource owner password grant type.
     */
    PASSWORD("password"),
    /**
     * For the client credentials grant type.
     */
    CLIENT_CREDENTIALS("client_credentials"),
    /**
     * For the refresh token grant type.
     */
    REFRESH_TOKEN("refresh_token"),
    /**
     * UMA ticket grant type.
     */
    UMA_TICKET("urn:ietf:params:oauth:grant-type:uma-ticket");

    private final String type;

    OAuth20GrantTypes(final String type) {
        this.type = type;
    }
}
