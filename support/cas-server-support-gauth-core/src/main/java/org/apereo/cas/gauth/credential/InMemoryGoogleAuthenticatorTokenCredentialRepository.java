package org.apereo.cas.gauth.credential;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.util.CollectionUtils;
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
    public Collection<? extends OneTimeTokenAccount> get(final String userName) {
        if (contains(userName)) {
            val account = this.accounts.get(userName);
            return decode(account);
        }
        return new ArrayList<>(0);
    }

    @Override
    public void save(final String userName, final String secretKey,
                     final int validationCode,
                     final List<Integer> scratchCodes) {
        val account = new OneTimeTokenAccount(userName, secretKey, validationCode, scratchCodes);
        val encoded = encode(account);
        this.accounts.put(account.getUsername(), CollectionUtils.wrapList(encoded));
    }

    @Override
    public OneTimeTokenAccount update(final OneTimeTokenAccount account) {
        val encoded = encode(account);
        if (accounts.containsKey(account.getUsername())) {
            val records = accounts.get(account.getUsername());
            records.stream()
                .filter(rec -> rec.getId()== account.getId())
                .findFirst()
                .ifPresent(act -> {
                    act.setSecretKey(account.getSecretKey());
                    act.setScratchCodes(account.getScratchCodes());
                    act.setValidationCode(account.getValidationCode());
                });
        }
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
        return accounts.values().stream().flatMap(List::stream).collect(Collectors.toList());
    }
}
