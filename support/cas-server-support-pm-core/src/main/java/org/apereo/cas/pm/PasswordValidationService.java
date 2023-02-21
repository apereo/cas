package org.apereo.cas.pm;

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
     * @param bean       the bean
     * @return true/false
     */
    boolean isValid(PasswordChangeRequest bean);
}
