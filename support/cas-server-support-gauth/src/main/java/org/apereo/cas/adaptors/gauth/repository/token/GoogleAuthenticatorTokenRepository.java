package org.apereo.cas.adaptors.gauth.repository.token;

import org.apereo.cas.GoogleAuthenticatorToken;

/**
 * This is {@link GoogleAuthenticatorTokenRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface GoogleAuthenticatorTokenRepository {

    /**
     * Clean expired/invalid tokens from the repository.
     * Clean up is likely run via a scheduler.
     */
    void clean();

    /**
     * Store token/code in the repository, marking it as invalid to be reused again.
     *
     * @param token the token
     */
    void store(GoogleAuthenticatorToken token);

    /**
     * Determine if the otp for user exists in repository.
     *
     * @param uid the uid
     * @param otp the otp
     * @return true/false.
     */
    boolean exists(String uid, Integer otp);
}
