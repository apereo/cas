package org.apereo.cas.gauth.credential;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.Getter;
import lombok.val;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

    private final Map<String, List<OneTimeTokenAccount>> accounts;

    public InMemoryGoogleAuthenticatorTokenCredentialRepository(final CipherExecutor<String, String> tokenCredentialCipher,
                                                                final IGoogleAuthenticator googleAuthenticator) {
        super(tokenCredentialCipher, googleAuthenticator);
        this.accounts = new ConcurrentHashMap<>();
    }

    @Override
    public OneTimeTokenAccount get(final String username, final long id) {
        return get(username).stream().filter(ac -> ac.getId() == id).findFirst().orElse(null);
    }

    @Override
    public OneTimeTokenAccount get(final long id) {
        return this.accounts.values().stream()
            .flatMap(List::stream)
            .filter(ac -> ac.getId() == id)
            .findFirst()
            .orElse(null);
    }

    @Override
    public Collection<? extends OneTimeTokenAccount> get(final String userName) {
        if (contains(userName)) {
            val account = this.accounts.get(userName.toLowerCase().trim());
            return decode(account);
        }
        return new ArrayList<>(0);
    }

    @Override
    public OneTimeTokenAccount save(final OneTimeTokenAccount account) {
        val encoded = encode(account);
        val records = accounts.getOrDefault(account.getUsername().trim().toLowerCase(), new ArrayList<>());
        records.add(encoded);
        accounts.put(account.getUsername(), records);
        return encoded;
    }

    @Override
    public OneTimeTokenAccount update(final OneTimeTokenAccount account) {
        val encoded = encode(account);
        if (accounts.containsKey(account.getUsername().toLowerCase().trim())) {
            val records = accounts.get(account.getUsername().toLowerCase().trim());
            records.stream()
                .filter(rec -> rec.getId() == account.getId())
                .findFirst()
                .ifPresent(act -> {
                    act.setSecretKey(account.getSecretKey());
                    act.setScratchCodes(account.getScratchCodes());
                    act.setValidationCode(account.getValidationCode());
                });
        }
        return encoded;
    }

    @Override
    public void deleteAll() {
        this.accounts.clear();
    }

    @Override
    public void delete(final String username) {
        this.accounts.remove(username.toLowerCase().trim());
    }

    @Override
    public void delete(final long id) {
        accounts.forEach((key, value) -> value.removeIf(d -> d.getId() == id));
    }

    @Override
    public long count() {
        return this.accounts.size();
    }

    @Override
    public long count(final String username) {
        return get(username.toLowerCase().trim()).size();
    }

    @Override
    public Collection<? extends OneTimeTokenAccount> load() {
        return accounts.values().stream().flatMap(List::stream).collect(Collectors.toList());
    }

    private boolean contains(final String username) {
        return this.accounts.containsKey(username.toLowerCase().trim());
    }
}
