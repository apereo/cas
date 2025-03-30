package org.apereo.cas.gauth.credential;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.util.concurrent.CasReentrantLock;
import org.apereo.cas.util.crypto.CipherExecutor;
import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.Getter;
import lombok.val;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * This is {@link InMemoryGoogleAuthenticatorTokenCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Getter
public class InMemoryGoogleAuthenticatorTokenCredentialRepository extends BaseGoogleAuthenticatorTokenCredentialRepository {
    private final CasReentrantLock lock = new CasReentrantLock();

    private final Map<String, List<OneTimeTokenAccount>> accounts;

    public InMemoryGoogleAuthenticatorTokenCredentialRepository(final CipherExecutor<String, String> tokenCredentialCipher,
                                                                final CipherExecutor<Number, Number> scratchCodesCipher,
                                                                final IGoogleAuthenticator googleAuthenticator) {
        super(tokenCredentialCipher, scratchCodesCipher, googleAuthenticator);
        this.accounts = new ConcurrentHashMap<>();
    }

    @Override
    public OneTimeTokenAccount get(final String username, final long id) {
        return lock.tryLock(() -> get(username).stream().filter(ac -> ac.getId() == id).findFirst().orElse(null));
    }

    @Override
    public OneTimeTokenAccount get(final long id) {
        return lock.tryLock(() -> accounts
            .values()
            .stream()
            .flatMap(List::stream)
            .filter(ac -> ac.getId() == id)
            .findFirst()
            .orElse(null));
    }

    @Override
    public Collection<? extends OneTimeTokenAccount> get(final String userName) {
        return lock.tryLock(() -> {
            if (contains(userName)) {
                val account = accounts.get(userName.toLowerCase(Locale.ENGLISH).trim());
                return decode(account);
            }
            return new ArrayList<>();
        });
    }

    @Override
    public OneTimeTokenAccount save(final OneTimeTokenAccount account) {
        account.assignIdIfNecessary();
        return lock.tryLock(() -> {
            val encoded = encode(account);
            val records = accounts.getOrDefault(account.getUsername().trim().toLowerCase(Locale.ENGLISH), new ArrayList<>());
            records.add(encoded);
            accounts.put(account.getUsername(), records);
            return encoded;
        });
    }

    @Override
    public OneTimeTokenAccount update(final OneTimeTokenAccount account) {
        return lock.tryLock(() -> {
            val encoded = encode(account);
            if (accounts.containsKey(account.getUsername().toLowerCase(Locale.ENGLISH).trim())) {
                val records = accounts.get(account.getUsername().toLowerCase(Locale.ENGLISH).trim());
                records.stream()
                    .filter(rec -> rec.getId() == account.getId())
                    .findFirst()
                    .ifPresent(act -> {
                        act.setSecretKey(account.getSecretKey());
                        act.setScratchCodes(account.getScratchCodes());
                        act.setValidationCode(account.getValidationCode());
                        act.setProperties(account.getProperties());
                    });
            }
            return encoded;
        });
    }

    @Override
    public void deleteAll() {
        lock.tryLock(__ -> accounts.clear());
    }

    @Override
    public void delete(final String username) {
        lock.tryLock(__ -> accounts.remove(username.toLowerCase(Locale.ENGLISH).trim()));
    }

    @Override
    public void delete(final long id) {
        lock.tryLock(__ -> accounts.forEach((key, value) -> value.removeIf(d -> d.getId() == id)));
    }

    @Override
    public long count() {
        return lock.tryLock(accounts::size);
    }

    @Override
    public long count(final String username) {
        return lock.tryLock(() -> get(username.toLowerCase(Locale.ENGLISH).trim()).size());
    }

    @Override
    public Collection<? extends OneTimeTokenAccount> load() {
        return lock.tryLock(() -> accounts.values().stream().flatMap(List::stream).collect(Collectors.toList()));
    }

    private boolean contains(final String username) {
        return accounts.containsKey(username.toLowerCase(Locale.ENGLISH).trim());
    }
}
