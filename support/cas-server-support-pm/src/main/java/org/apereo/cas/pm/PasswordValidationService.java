package org.apereo.cas.pm;

import org.apereo.cas.authentication.UsernamePasswordCredential;

/**
 * This is {@link PasswordValidationService}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@FunctionalInterface
public interface PasswordValidationService {
    /**
     * Validate password.
     *
     * @param c    the c
     * @param bean the bean
     * @return the boolean
     */
    boolean isValid(UsernamePasswordCredential c, PasswordChangeBean bean);
}
