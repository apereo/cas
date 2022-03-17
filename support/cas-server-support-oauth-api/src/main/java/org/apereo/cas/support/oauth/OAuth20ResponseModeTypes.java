package org.apereo.cas.support.oauth;

import lombok.Getter;

/**
 * The OAuth response mode types (on the authorize request).
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Getter
public enum OAuth20ResponseModeTypes {
    /**
     * No response mode.
     */
    NONE("none"),

    /**
     * Query mode.
     * In this mode, Authorization Response parameters are encoded in the query
     * string added to the redirect_uri when redirecting back to the Client.
     */
    QUERY("query"),
    /**
     * Fragment mode.
     * In this mode, Authorization Response parameters are encoded in the
     * fragment added to the redirect_uri when redirecting back to the Client.
     */
    FRAGMENT("fragment"),
    /**
     * {@code form_post} response mode.
     */
    FORM_POST("form_post");

    private final String type;

    OAuth20ResponseModeTypes(final String type) {
        this.type = type;
    }
}
