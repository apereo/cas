package org.apereo.cas.otp.repository.credentials;

import org.apereo.cas.authentication.OneTimeTokenAccount;

import java.util.Collection;
import java.util.List;

/**
 * This is {@link OneTimeTokenCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface OneTimeTokenCredentialRepository {
    /**
     * Gets secret.
     *
     * @param username the username
     * @return the secret
     */
    OneTimeTokenAccount get(String username);

    /**
     * Load all accounts into collection.
     *
     * @return the collection
     */
    Collection<? extends OneTimeTokenAccount> load();

    /**
     * Save.
     *
     * @param userName       the user name
     * @param secretKey      the secret key
     * @param validationCode the validation code
     * @param scratchCodes   the scratch codes
     */
    void save(String userName, String secretKey, int validationCode, List<Integer> scratchCodes);

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
     * Count all records.
     *
     * @return the long
     */
    long count();
}
