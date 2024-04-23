package org.apereo.cas.api;

import org.apereo.cas.impl.token.PasswordlessAuthenticationToken;

import java.util.Optional;

/**
 * This is {@link PasswordlessTokenRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public interface PasswordlessTokenRepository {
    /**
     * Bean name.
     */
    String BEAN_NAME = "passwordlessTokenRepository";

    /**
     * Create token string.
     *
     * @param username the username
     * @return the string
     */
    PasswordlessAuthenticationToken createToken(PasswordlessUserAccount username, PasswordlessAuthenticationRequest passwordlessRequest);

    /**
     * Find token string.
     *
     * @param username the username
     * @return the string
     */
    Optional<PasswordlessAuthenticationToken> findToken(String username);

    /**
     * Encode token string.
     *
     * @param token the token
     * @return the string
     */
    String encodeToken(PasswordlessAuthenticationToken token);

    /**
     * Delete token.
     *
     * @param username the username
     */
    void deleteTokens(String username);

    /**
     * Delete token.
     *
     * @param token the token
     */
    void deleteToken(PasswordlessAuthenticationToken token);


    /**
     * Save token passwordless authentication token.
     *
     * @param passwordlessAccount the passwordless account
     * @param passwordlessRequest the passwordless request
     * @param token               the token
     * @return the passwordless authentication token
     */
    PasswordlessAuthenticationToken saveToken(PasswordlessUserAccount passwordlessAccount,
                                              PasswordlessAuthenticationRequest passwordlessRequest,
                                              PasswordlessAuthenticationToken token);

    /**
     * Clean the repository to remove expired records.
     */
    void clean();
}
