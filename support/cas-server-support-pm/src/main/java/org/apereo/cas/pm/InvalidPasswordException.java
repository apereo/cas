package org.apereo.cas.pm;

/**
 * Raised by {@link PasswordManagementService} if it is also responsible for validating
 * passwords and a new password fails validation.
 *
 * @author Marcus Watkins
 * @since 5.2.0
 */

public class InvalidPasswordException extends RuntimeException {

    private static final long serialVersionUID = 458954862481278L;
}
