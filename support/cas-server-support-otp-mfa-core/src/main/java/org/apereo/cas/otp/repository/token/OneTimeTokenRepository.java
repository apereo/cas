package org.apereo.cas.otp.repository.token;

import org.apereo.cas.authentication.OneTimeToken;

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
     * @return true /false.
     */
    default boolean exists(final String uid, final Integer otp) {
        return get(uid, otp) != null;
    }

    /**
     * Get one time token.
     *
     * @param uid the uid
     * @param otp the opt
     * @return the one time token
     */
    OneTimeToken get(String uid, Integer otp);

    /**
     * Remove the token.
     *
     * @param uid the uid
     * @param otp the otp
     */
    void remove(String uid, Integer otp);

    /**
     * Remove tokens for user.
     *
     * @param uid the uid
     */
    void remove(String uid);

    /**
     * Remove otp.
     *
     * @param otp the otp
     */
    void remove(Integer otp);

    /**
     * Remove all.
     */
    void removeAll();

    /**
     * Count tokens for user.
     *
     * @param uid the uid
     * @return the long
     */
    long count(String uid);

    /**
     * Count all records.
     *
     * @return the long
     */
    long count();
}
