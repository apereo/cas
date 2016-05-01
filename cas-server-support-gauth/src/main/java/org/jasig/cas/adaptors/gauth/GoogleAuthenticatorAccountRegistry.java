package org.jasig.cas.adaptors.gauth;

import com.warrenstrange.googleauth.ICredentialRepository;

/**
 * General contract that allows one to determine whether
 * a particular google authenticator account
 * is allowed to participate in the authentication.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface GoogleAuthenticatorAccountRegistry extends ICredentialRepository {
    /**
     * Contains username?
     *
     * @param username the userename
     * @return the boolean
     */
    boolean contains(String username);

    /**
     * Save.
     *
     * @param username the username
     * @param account  the account
     */
    void save(String username, GoogleAuthenticatorAccount account);

    /**
     * Gets account.
     *
     * @param username the username
     * @return the account
     */
    GoogleAuthenticatorAccount get(String username);

    /**
     * Remove.
     *
     * @param username the username
     */
    void remove(String username);

    /**
     * Clear.
     */
    void clear();
}
