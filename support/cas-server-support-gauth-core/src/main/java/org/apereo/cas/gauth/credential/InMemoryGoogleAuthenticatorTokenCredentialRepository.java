package org.apereo.cas.gauth.credential;

import module java.base;
import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.gauth.CasGoogleAuthenticator;
import org.apereo.cas.util.concurrent.CasReentrantLock;
import org.apereo.cas.util.crypto.CipherExecutor;
import lombok.Getter;
import lombok.val;

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
                                                                final CasGoogleAuthenticator googleAuthenticator) {
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
            .map(this::decode)
            .orElse(null));
    }

    /**
     * Fetch the accounts registered for the user. A lock that cannot be acquired is raised as an error rather than
     * reported as "no accounts", because an empty result sends the user to device registration.
     *
     * @param userName the username
     * @return the accounts registered for the user
     */
    @Override
    public Collection<? extends OneTimeTokenAccount> get(final String userName) {
        val result = lock.<Collection<? extends OneTimeTokenAccount>>tryLock(() -> {
            if (contains(userName)) {
                val account = accounts.get(userName.toLowerCase(Locale.ENGLISH).trim());
                return decode(account);
            }
            return new ArrayList<>();
        });
        return Objects.requireNonNull(result, () -> "Unable to read accounts for " + userName);
    }

    /**
     * Save the account, replacing a stored account with the same id rather than adding a duplicate.
     *
     * @param account the account
     * @return the encoded account as stored
     */
    @Override
    public OneTimeTokenAccount save(final OneTimeTokenAccount account) {
        account.assignIdIfNecessary();
        return lock.tryLock(() -> {
            val encoded = encode(account);
            val records = accounts.getOrDefault(encoded.getUsername(), new ArrayList<>());
            records.removeIf(rec -> rec.getId() == encoded.getId());
            records.add(encoded);
            accounts.put(encoded.getUsername(), records);
            return encoded;
        });
    }

    /**
     * Update the stored account with the same id, or add the account when none is stored,
     * so that every backend treats an update of a missing device as an insert.
     *
     * @param account the account
     * @return the encoded account as stored
     */
    @Override
    public OneTimeTokenAccount update(final OneTimeTokenAccount account) {
        return lock.tryLock(() -> {
            val encoded = encode(account);
            val records = accounts.computeIfAbsent(encoded.getUsername(), _ -> new ArrayList<>());
            records.stream()
                .filter(rec -> rec.getId() == encoded.getId())
                .findFirst()
                .ifPresentOrElse(act -> {
                    act.setSecretKey(encoded.getSecretKey());
                    act.setScratchCodes(encoded.getScratchCodes());
                    act.setValidationCode(encoded.getValidationCode());
                    act.setProperties(new ArrayList<>(encoded.getProperties()));
                    act.setLastUsedDateTime(encoded.getLastUsedDateTime());
                }, () -> records.add(encoded));
            return encoded;
        });
    }

    @Override
    public void deleteAll() {
        lock.tryLock(_ -> accounts.clear());
    }

    @Override
    public void delete(final String username) {
        lock.tryLock(_ -> accounts.remove(username.toLowerCase(Locale.ENGLISH).trim()));
    }

    @Override
    public void delete(final long id) {
        lock.tryLock(_ -> accounts.forEach((key, value) -> value.removeIf(d -> d.getId() == id)));
    }

    @Override
    public long count() {
        return lock.tryLock(() -> accounts.values().stream().mapToLong(List::size).sum());
    }

    @Override
    public long count(final String username) {
        return lock.tryLock(() -> get(username.toLowerCase(Locale.ENGLISH).trim()).size());
    }

    @Override
    public Collection<? extends OneTimeTokenAccount> load() {
        return lock.tryLock(() -> accounts.values().stream().flatMap(List::stream).map(this::decode).collect(Collectors.toList()));
    }

    private boolean contains(final String username) {
        return accounts.containsKey(username.toLowerCase(Locale.ENGLISH).trim());
    }
}
