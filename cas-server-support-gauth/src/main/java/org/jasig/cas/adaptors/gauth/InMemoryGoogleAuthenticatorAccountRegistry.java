package org.jasig.cas.adaptors.gauth;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is {@link InMemoryGoogleAuthenticatorAccountRegistry}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Component("defaultGoogleAuthenticatorAccountRegistry")
public class InMemoryGoogleAuthenticatorAccountRegistry implements GoogleAuthenticatorAccountRegistry {
    
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

    @Override
    public void save(final String username, final GoogleAuthenticatorAccount account) {
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

    @Override
    public boolean contains(final String username) {
        return this.accounts.containsKey(username);
    }

    @Override
    public void remove(final String username) {
        this.accounts.remove(username);
    }

    @Override
    public void clear() {
        this.accounts.clear();
    }

    @Override
    public GoogleAuthenticatorAccount get(final String username) {
        return this.accounts.get(username);
    }
}
