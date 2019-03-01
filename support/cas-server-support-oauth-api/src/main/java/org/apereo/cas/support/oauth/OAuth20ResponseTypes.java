package org.apereo.cas.support.oauth;

import lombok.Getter;

/**
 * The OAuth response types (on the authorize request).
 *
 * @author Jerome Leleu
 * @since 5.0.0
 */
@Getter
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
    ID_TOKEN("id_token");


    private final String type;

    OAuth20ResponseTypes(final String type) {
        this.type = type;
    }
}
