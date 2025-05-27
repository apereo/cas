package org.apereo.cas.pm;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.With;
import lombok.experimental.SuperBuilder;
import org.springframework.util.LinkedMultiValueMap;
import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link PasswordManagementQuery}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@SuperBuilder
@Getter
@ToString(exclude = {"record", "securityQuestions"})
@EqualsAndHashCode(of = "username")
@AllArgsConstructor
@With
public class PasswordManagementQuery implements Serializable {
    @Serial
    private static final long serialVersionUID = -769463174930246283L;

    private final String email;

    private final String username;

    private final String phoneNumber;

    @Builder.Default
    private final LinkedMultiValueMap<String, String> securityQuestions = new LinkedMultiValueMap<>();

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
    @CanIgnoreReturnValue
    public PasswordManagementQuery attribute(final String attributeName, final Object value) {
        record.add(attributeName, value);
        return this;
    }

    /**
     * Security question.
     *
     * @param question the question
     * @param answer   the answer
     * @return the password management query
     */
    @CanIgnoreReturnValue
    public PasswordManagementQuery securityQuestion(final String question, final String answer) {
        this.securityQuestions.add(question, answer);
        return this;
    }
}
