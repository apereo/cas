package org.apereo.cas.otp.repository.token;

/**
 * This is {@link OneTimeTokenRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface OneTimeTokenRepository {

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
    void store(OneTimeToken token);

    /**
     * Determine if the otp for user exists in repository.
     *
     * @param uid the uid
     * @param otp the otp
     * @return true/false.
     */
    boolean exists(String uid, Integer otp);
}
