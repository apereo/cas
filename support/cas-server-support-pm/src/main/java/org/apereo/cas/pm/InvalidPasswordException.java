package org.apereo.cas.pm;

/**
 * Raised by {@link PasswordManagementService} if it is also responsible for validating
 * passwords and a new password fails validation.
 *
 * @author Marcus Watkins
 * @since 5.2.0
 */

public class InvalidPasswordException extends RuntimeException {

    private static final long serialVersionUID = 458954862481279L;

    private final String code;
    private final String validationMessage;
    private final Object[] params;

    public InvalidPasswordException() {
        code = null;
        validationMessage = null;
        params = null;
    }

    public InvalidPasswordException(final String code, final String validationMessage, final Object... params) {
        this.code = code;
        this.validationMessage = validationMessage;
        this.params = params;
    }

    public String getCode() {
        return code;
    }

    public String getValidationMessage() {
        return validationMessage;
    }

    public Object[] getParams() {
        return params;
    }

}
