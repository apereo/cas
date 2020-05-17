package org.apereo.cas.otp.validator;

import org.apereo.cas.authentication.OneTimeTokenAccount;

/**
 * This is {@link OneTimeTokenAccountValidator}.
 *
 * Provides a common interface for objects that can
 * validate OTP credentials.
 *
 * @author Hayden Sartoris
 * @since 6.2.0-RC5
 */
@FunctionalInterface
public interface OneTimeTokenAccountValidator {

    /**
     * Is the token valid for the given account?
     *
     * @param account the OTP account
     * @param token   the token to validate
     * @return true/false
     */
    boolean isValid(OneTimeTokenAccount account, int token);

}
