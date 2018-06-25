package org.apereo.cas.otp.repository.credentials;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.authentication.OneTimeTokenAccount;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is {@link BaseInMemoryOneTimeTokenCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public abstract class BaseInMemoryOneTimeTokenCredentialRepository extends BaseOneTimeTokenCredentialRepository {

    private final Map<String, OneTimeTokenAccount> accounts;

    public BaseInMemoryOneTimeTokenCredentialRepository(final CipherExecutor<String, String> tokenCredentialCipher) {
        super(tokenCredentialCipher);
        this.accounts = new ConcurrentHashMap<>();
    }

    @Override
    public OneTimeTokenAccount get(final String userName) {
        if (contains(userName)) {
            final var account = this.accounts.get(userName);
            return decode(account);
        }
        return null;
    }

    @Override
    public void save(final String userName, final String secretKey,
                     final int validationCode,
                     final List<Integer> scratchCodes) {
        final var account = new OneTimeTokenAccount(userName, secretKey, validationCode, scratchCodes);
        update(account);
    }

    @Override
    public OneTimeTokenAccount update(final OneTimeTokenAccount account) {
        final var encoded = encode(account);
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
}
