package org.apereo.cas.otp.validator;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.authentication.credential.OneTimeTokenCredential;

/**
 * This is {@link OneTimeTokenAccountValidator}.
 *
 * Provides a common interface for objects that can
 * validate OTP credentials.
 *
 * @author Hayden Sartoris
 * @since 6.2.0
 */
public interface OneTimeTokenAccountValidator {

    /**
     * Is the token valid for the given account?
     *
     * @param account the OTP account
     * @param token   the token to validate
     * @return true/false
     */
    boolean isValid(OneTimeTokenAccount account, int token);

    /**
     * Parse presumed OTP from a Credential.
     *
     * @param credential the Credential presumed to contain the token
     * @return the integer token
     */
    default int parseToken(final OneTimeTokenCredential credential) {
        try {
            return Integer.parseInt(credential.getToken());
        } catch (final NumberFormatException e) {
            return -1;
        }
    }
}
