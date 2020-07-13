package org.apereo.cas.pm;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;

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
     * @return true/false
     */
    boolean isValid(UsernamePasswordCredential c, PasswordChangeRequest bean);
}
