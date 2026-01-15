package org.apereo.cas.pm;

import module java.base;
import org.apereo.cas.authentication.RootCasException;
import lombok.Getter;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Raised by {@link PasswordManagementService} if it is also responsible for validating
 * passwords and a new password fails validation.
 *
 * @author Marcus Watkins
 * @since 5.2.0
 */
@Getter
public class InvalidPasswordException extends RootCasException {

    @Serial
    private static final long serialVersionUID = 458954862481279L;

    private static final String CODE = "pm.passwordFailedCriteria";

    private final String validationMessage;

    private final Object[] params;

    public InvalidPasswordException() {
        this(null, ArrayUtils.EMPTY_OBJECT_ARRAY);
    }

    public InvalidPasswordException(final String validationMessage, final Object[] params) {
        super(CODE);
        this.validationMessage = validationMessage;
        this.params = params;
    }

    protected InvalidPasswordException(final String message) {
        super(CODE, message);
        this.validationMessage = null;
        this.params = null;
    }
}
