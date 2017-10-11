package org.apereo.cas.otp.repository.credentials;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is {@link BaseInMemoryOneTimeTokenCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public abstract class BaseInMemoryOneTimeTokenCredentialRepository extends BaseOneTimeTokenCredentialRepository {

    private final Map<String, OneTimeTokenAccount> accounts;

    /**
     * Instantiates a new In memory google authenticator account registry.
     */
    public BaseInMemoryOneTimeTokenCredentialRepository() {
        this.accounts = new ConcurrentHashMap<>();
    }

    @Override
    public OneTimeTokenAccount get(final String userName) {
        if (contains(userName)) {
            return this.accounts.get(userName);
        }
        return null;
    }

    @Override
    public void save(final String userName, final String secretKey,
                     final int validationCode,
                     final List<Integer> scratchCodes) {
        final OneTimeTokenAccount account = new OneTimeTokenAccount(userName, secretKey, validationCode, scratchCodes);
        this.accounts.put(userName, account);
    }

    private boolean contains(final String username) {
        return this.accounts.containsKey(username);
    }

    /**
     * Remove.
     *
     * @param username the username
     */
    public void remove(final String username) {
        this.accounts.remove(username);
    }
}
