package org.apereo.cas.api;

import java.util.Optional;

/**
 * This is {@link PasswordlessTokenRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public interface PasswordlessTokenRepository {

    /**
     * Create token string.
     *
     * @param username the username
     * @return the string
     */
    String createToken(String username);

    /**
     * Find token string.
     *
     * @param username the username
     * @return the string
     */
    Optional<String> findToken(String username);

    /**
     * Delete token.
     *
     * @param username the username
     */
    void deleteTokens(String username);

    /**
     * Delete token.
     *
     * @param username the username
     * @param token    the token
     */
    void deleteToken(String username, String token);

    /**
     * Save token.
     *
     * @param username the username
     * @param token    the token
     */
    void saveToken(String username, String token);

    /**
     * Clean the repository to remove expired records.
     */
    void clean();
}
