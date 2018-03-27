package org.apereo.cas.pm;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.Getter;

/**
 * Raised by {@link PasswordManagementService} if it is also responsible for validating
 * passwords and a new password fails validation.
 *
 * @author Marcus Watkins
 * @since 5.2.0
 */
@Slf4j
@Getter
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class InvalidPasswordException extends RuntimeException {

    private static final long serialVersionUID = 458954862481279L;

    private final String code;

    private final String validationMessage;

    private final Object[] params;
}
