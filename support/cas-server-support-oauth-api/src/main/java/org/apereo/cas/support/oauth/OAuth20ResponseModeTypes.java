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
     * {@code form_post} response mode.
     */
    FORM_POST("form_post");

    private final String type;

    OAuth20ResponseModeTypes(final String type) {
        this.type = type;
    }
}
