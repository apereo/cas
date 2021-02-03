package org.apereo.cas.pm;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.util.LinkedMultiValueMap;

import java.io.Serializable;

/**
 * This is {@link PasswordManagementQuery}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@SuperBuilder
@Getter
@ToString(exclude = "record")
public class PasswordManagementQuery implements Serializable {
    private static final long serialVersionUID = -769463174930246283L;

    private final String email;

    private final String username;

    private final String phoneNumber;

    @Builder.Default
    private final LinkedMultiValueMap<String, Object> record = new LinkedMultiValueMap<>();

    /**
     * Find attribute.
     *
     * @param <T>           the type parameter
     * @param attributeName the attribute name
     * @param clazz         the clazz
     * @return the string
     */
    public <T> T find(final String attributeName, final Class<T> clazz) {
        return clazz.cast(record.getFirst(attributeName));
    }

    /**
     * Add.
     *
     * @param attributeName the attribute name
     * @param value         the value
     * @return the user record context
     */
    public PasswordManagementQuery add(final String attributeName, final Object value) {
        record.add(attributeName, value);
        return this;
    }


}
