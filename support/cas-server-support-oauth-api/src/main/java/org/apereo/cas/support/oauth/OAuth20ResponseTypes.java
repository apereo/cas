package org.apereo.cas.support.oauth;

import lombok.Getter;

import java.util.Arrays;

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

    /**
     * Returns the enum value corresponding to the passed type value.
     *
     * @param type - the response_type value
     * @return - OAuth20ResponseType enum
     */
    public static OAuth20ResponseTypes valueByType(final String type) {
        return Arrays.stream(OAuth20ResponseTypes.values())
                .filter(v -> v.getType().equalsIgnoreCase(type))
                .findFirst().orElse(null);
    }
}
