package org.apereo.cas.otp.repository.credentials;

import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.OneTimeTokenAccount;

import lombok.val;

import java.util.Collection;
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

    public BaseInMemoryOneTimeTokenCredentialRepository(final CipherExecutor<String, String> tokenCredentialCipher) {
        super(tokenCredentialCipher);
        this.accounts = new ConcurrentHashMap<>();
    }

    @Override
    public OneTimeTokenAccount get(final String userName) {
        if (contains(userName)) {
            val account = this.accounts.get(userName);
            return decode(account);
        }
        return null;
    }

    @Override
    public void save(final String userName, final String secretKey,
                     final int validationCode,
                     final List<Integer> scratchCodes) {
        val account = new OneTimeTokenAccount(userName, secretKey, validationCode, scratchCodes);
        update(account);
    }

    @Override
    public OneTimeTokenAccount update(final OneTimeTokenAccount account) {
        val encoded = encode(account);
        this.accounts.put(account.getUsername(), encoded);
        return encoded;
    }

    private boolean contains(final String username) {
        return this.accounts.containsKey(username);
    }

    @Override
    public void deleteAll() {
        this.accounts.clear();
    }

    @Override
    public void delete(final String username) {
        this.accounts.remove(username);
    }

    @Override
    public long count() {
        return this.accounts.size();
    }

    @Override
    public Collection<? extends OneTimeTokenAccount> load() {
        return accounts.values();
    }
}
