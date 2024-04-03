package org.apereo.cas.support.oauth;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The OAuth grant types (on the access token request).
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
@Getter
@RequiredArgsConstructor
public enum OAuth20GrantTypes {

    /**
     * Indicates the absence of invalidity of a grant type.
     */
    NONE("none"),
    /**
     * Grant type designated for device code flows.
     */
    DEVICE_CODE("urn:ietf:params:oauth:grant-type:device_code"),
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
     * For the token exchange grant type.
     */
    TOKEN_EXCHANGE("urn:ietf:params:oauth:grant-type:token-exchange"),
    /**
     * UMA ticket grant type.
     */
    UMA_TICKET("urn:ietf:params:oauth:grant-type:uma-ticket"),
    /**
     * CIBA grant type.
     */
    CIBA("urn:openid:params:grant-type:ciba");
    
    private final String type;

}
