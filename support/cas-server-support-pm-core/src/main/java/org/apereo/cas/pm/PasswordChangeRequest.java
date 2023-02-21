package org.apereo.cas.pm;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;

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
    private char[] currentPassword;

    private @Size(min = 1, message = "required.password") char[] password;

    private @Size(min = 1, message = "required.confirmedPassword") char[] confirmedPassword;

    /**
     * To current/old password string.
     *
     * @return the string
     */
    public String toCurrentPassword() {
        return new String(this.currentPassword);
    }

    /**
     * To new password string.
     *
     * @return the string
     */
    public String toPassword() {
        return this.password == null ? null : new String(this.password);
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
