package org.apereo.cas.support.oauth;

import module java.base;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The OAuth response types (on the authorize request).
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
@Getter
@RequiredArgsConstructor
public enum OAuth20ResponseTypes {

    /**
     * For authorization response type.
     */
    CODE("code"),
    /**
     * For indication of invalid or absent response type.
     */
    NONE("none"),
    /**
     * For implicit response type.
     */
    TOKEN("token"),
    /**
     * For device_code response type.
     */
    DEVICE_CODE("device_code"),
    /**
     * For implicit response type.
     */
    IDTOKEN_TOKEN("id_token token"),
    /**
     * For implicit response type.
     */
    TOKEN_IDTOKEN("token id_token"),
    /**
     * For implicit response type.
     */
    ID_TOKEN("id_token");

    private final String type;
}
