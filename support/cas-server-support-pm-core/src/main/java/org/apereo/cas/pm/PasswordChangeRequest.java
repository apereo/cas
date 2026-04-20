package org.apereo.cas.pm;

import module java.base;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.Size;

/**
 * This is {@link PasswordChangeRequest}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class PasswordChangeRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 8885460875620586503L;

    private String username;

    private @Size(min = 1, message = "required.password") char[] password;

    private @Size(min = 1, message = "required.confirmedPassword") char[] confirmedPassword;

    /**
     * To new password string.
     *
     * @return the string
     */
    public String toPassword() {
        return Optional.ofNullable(this.password).map(String::new).orElse(null);
    }

    /**
     * To new/confirmed password string.
     *
     * @return the string
     */
    public String toConfirmedPassword() {
        return new String(this.confirmedPassword);
    }
}
