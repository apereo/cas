package org.apereo.cas.otp.repository.credentials;

import org.apereo.cas.authentication.OneTimeTokenAccount;

import java.util.Collection;

/**
 * This is {@link OneTimeTokenCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface OneTimeTokenCredentialRepository {
    /**
     * Get one time token account.
     *
     * @param id the id
     * @return the one time token account
     */
    OneTimeTokenAccount get(long id);

    /**
     * Get one time token account.
     *
     * @param username the username
     * @param id       the id
     * @return the one time token account
     */
    OneTimeTokenAccount get(String username, long id);

    /**
     * Gets accounts.
     *
     * @param username the username
     * @return the secret
     */
    Collection<? extends OneTimeTokenAccount> get(String username);

    /**
     * Load all accounts into collection.
     *
     * @return the collection
     */
    Collection<? extends OneTimeTokenAccount> load();

    /**
     * Save.
     *
     * @param account the account
     * @return one time token account
     */
    OneTimeTokenAccount save(OneTimeTokenAccount account);

    /**
     * Create one time token account.
     *
     * @param username the username
     * @return the one time token account
     */
    OneTimeTokenAccount create(String username);

    /**
     * Update an existing account.
     *
     * @param account the registration record
     * @return the one time token account
     */
    OneTimeTokenAccount update(OneTimeTokenAccount account);

    /**
     * Delete all tokens in the repository.
     */
    void deleteAll();

    /**
     * Delete.
     *
     * @param username the username
     */
    void delete(String username);

    /**
     * Delete by id.
     *
     * @param id device identifier
     */
    void delete(long id);

    /**
     * Count all records.
     *
     * @return the long
     */
    long count();

    /**
     * Count records for username.
     *
     * @param username the username
     * @return the count
     */
    long count(String username);
}
