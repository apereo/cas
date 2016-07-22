package org.apereo.cas.adaptors.gauth;

import com.warrenstrange.googleauth.ICredentialRepository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is {@link InMemoryGoogleAuthenticatorAccountRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class InMemoryGoogleAuthenticatorAccountRegistry implements ICredentialRepository {

    private Map<String, GoogleAuthenticatorAccount> accounts;

    /**
     * Instantiates a new In memory google authenticator account registry.
     */
    public InMemoryGoogleAuthenticatorAccountRegistry() {
        this.accounts = new ConcurrentHashMap<>();
    }

    @Override
    public String getSecretKey(final String userName) {
        if (contains(userName)) {
            return this.accounts.get(userName).getSecretKey();
        }
        return null;
    }

    private void save(final String username, final GoogleAuthenticatorAccount account) {
        saveUserCredentials(username, account.getSecretKey(), account.getValidationCode(), account.getScratchCodes());
    }

    @Override
    public void saveUserCredentials(final String userName, final String secretKey,
                                    final int validationCode,
                                    final List<Integer> scratchCodes) {
        final GoogleAuthenticatorAccount account = new GoogleAuthenticatorAccount();
        account.setScratchCodes(scratchCodes);
        account.setValidationCode(validationCode);
        account.setSecretKey(secretKey);
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

    /**
     * Clear.
     */
    private void clear() {
        this.accounts.clear();
    }

    private GoogleAuthenticatorAccount get(final String username) {
        return this.accounts.get(username);
    }
}
