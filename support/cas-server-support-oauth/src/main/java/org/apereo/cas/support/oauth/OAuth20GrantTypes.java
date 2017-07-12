package org.apereo.cas.support.oauth;

/**
 * The OAuth grant types (on the access token request).
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
public enum OAuth20GrantTypes {
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

    /** The implicit flow does not have a grant_type associated with it.
     * This is more an implementation detail that explicitly defines one for consistency.
     */
    IMPLICIT("implicit");

    private final String type;

    OAuth20GrantTypes(final String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
