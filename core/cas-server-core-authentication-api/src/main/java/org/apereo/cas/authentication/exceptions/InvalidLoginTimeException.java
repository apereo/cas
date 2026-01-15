package org.apereo.cas.authentication.exceptions;

import module java.base;
import lombok.NoArgsConstructor;

/**
 * Describes an error condition where authentication occurs at a time that is disallowed by security policy
 * applied to the underlying user account.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@NoArgsConstructor
public class InvalidLoginTimeException extends AccountException {

    @Serial
    private static final long serialVersionUID = -6699752791525619208L;

    public InvalidLoginTimeException(final String message) {
        super(message);
    }

}
