package org.apereo.cas.otp.validator;

import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.authentication.credential.OneTimeTokenCredential;

/**
 * This is {@link OneTimeTokenAccountValidator}.
 *
 * Provides a common interface for objects that can
 * validate OTP credentials.
 *
 * @author Hayden Sartoris
 * @since 6.2.0-RC5
 */
public interface OneTimeTokenAccountValidator {

    /**
     * Is the token valid for the given account?
     *
     * Assumes parsing logic has taken place.
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
    default int parseToken(Credential credential) throws IllegalArgumentException {
        try {
            OneTimeTokenCredential cred = (OneTimeTokenCredential) credential;
            return Integer.parseInt(cred.getToken());
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Provided credential could not be cast to OneTimeTokenCredential", e);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Token could not be parsed: " + ((OneTimeTokenCredential) credential).getToken(), e);
        }
    }
}
