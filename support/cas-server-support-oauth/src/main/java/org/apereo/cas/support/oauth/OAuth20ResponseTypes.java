package org.apereo.cas.support.oauth;

/**
 * The OAuth response types (on the authorize request).
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
public enum OAuth20ResponseTypes {

    /**
     * For authorization grant type.
     */
    CODE("code"),
    /**
     * For implicit grant type.
     */
    TOKEN("token"),
    /**
     * For implicit grant type.
     */
    IDTOKEN_TOKEN("id_token token");

    private final String type;

    OAuth20ResponseTypes(final String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
