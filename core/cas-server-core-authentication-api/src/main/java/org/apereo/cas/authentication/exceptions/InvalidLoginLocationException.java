package org.apereo.cas.authentication.exceptions;

import module java.base;
import lombok.NoArgsConstructor;

/**
 * Describes an error condition where authentication occurs from a location that is disallowed by security policy
 * applied to the underlying user account.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@NoArgsConstructor
public class InvalidLoginLocationException extends AccountException {

    @Serial
    private static final long serialVersionUID = 5745711263227480194L;

    public InvalidLoginLocationException(final String message) {
        super(message);
    }
}
