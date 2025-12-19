package org.apereo.cas.support.oauth;

import module java.base;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The OAuth response mode types (on the authorize request).
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Getter
@RequiredArgsConstructor
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
     * {@code query.jwt} response mode.
     */
    QUERY_JWT("query.jwt"),
    /**
     * {@code fragment.jwt} response mode.
     */
    FRAGMENT_JWT("fragment.jwt"),

    /**
     * {@code form_post.jwt} response mode.
     */
    FORM_POST_JWT("form_post.jwt"),

    /**
     * {@code form_post} response mode.
     */
    FORM_POST("form_post");

    private final String type;
}
