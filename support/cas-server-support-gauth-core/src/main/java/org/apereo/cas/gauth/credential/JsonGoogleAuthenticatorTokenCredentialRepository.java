package org.apereo.cas.gauth.credential;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.gauth.CasGoogleAuthenticator;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.concurrent.CasReentrantLock;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.StringSerializer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.io.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This is {@link JsonGoogleAuthenticatorTokenCredentialRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Getter
@Slf4j
public class JsonGoogleAuthenticatorTokenCredentialRepository extends BaseGoogleAuthenticatorTokenCredentialRepository {
    private final CasReentrantLock lock = new CasReentrantLock();

    private final Resource location;

    private final StringSerializer<Map<String, List<OneTimeTokenAccount>>> serializer;

    public JsonGoogleAuthenticatorTokenCredentialRepository(
        final Resource location,
        final CasGoogleAuthenticator googleAuthenticator,
        final CipherExecutor<String, String> tokenCredentialCipher,
        final CipherExecutor<Number, Number> scratchCodesCipher,
        final StringSerializer<Map<String, List<OneTimeTokenAccount>>> serializer) {
        super(tokenCredentialCipher, scratchCodesCipher, googleAuthenticator);
        this.location = location;
        this.serializer = serializer;
    }

    @Override
    public OneTimeTokenAccount get(final long id) {
        return lock.tryLock(() -> {
            val accounts = readAccountsFromJsonRepository();
            return accounts.values()
                .stream()
                .flatMap(List::stream)
                .filter(ac -> ac.getId() == id)
                .findFirst()
                .map(this::decode)
                .orElse(null);
        });
    }

    @Override
    public OneTimeTokenAccount get(final String username, final long id) {
        return lock.tryLock(() -> get(username)
            .stream()
            .filter(ac -> ac.getId() == id)
            .findFirst()
            .map(this::decode)
            .orElse(null));
    }

    @Override
    public Collection<? extends OneTimeTokenAccount> get(final String username) {
        return lock.tryLock(() -> {
            try {
                if (!location.getFile().exists()) {
                    LOGGER.warn("JSON account repository file [{}] is not found.", location.getFile());
                    return new ArrayList<>();
                }

                if (location.getFile().length() <= 0) {
                    LOGGER.debug("JSON account repository file location [{}] is empty.", location.getFile());
                    return new ArrayList<>();
                }
                val map = serializer.from(location.getFile());
                if (map == null) {
                    LOGGER.debug("JSON account repository file [{}] is empty.", location.getFile());
                    return new ArrayList<>();
                }

                val account = map.get(username.trim().toLowerCase(Locale.ENGLISH));
                if (account != null) {
                    return decode(account);
                }
            } catch (final Exception e) {
                LoggingUtils.error(LOGGER, e);
            }
            return new ArrayList<>();
        });
    }

    @Override
    public Collection<? extends OneTimeTokenAccount> load() {
        return lock.tryLock(() -> {
            try {
                return readAccountsFromJsonRepository()
                    .values()
                    .stream()
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
            } catch (final Exception e) {
                LoggingUtils.error(LOGGER, e);
            }
            return new ArrayList<>();
        });
    }

    @Override
    public OneTimeTokenAccount save(final OneTimeTokenAccount account) {
        return lock.tryLock(() -> {
            try {
                account.assignIdIfNecessary();
                LOGGER.debug("Storing google authenticator account for [{}]", account.getUsername());
                val accounts = readAccountsFromJsonRepository();
                LOGGER.debug("Found [{}] account(s) and added google authenticator account for [{}]",
                    accounts.size(), account.getUsername());
                val encoded = encode(account);
                val records = accounts.getOrDefault(account.getUsername().trim().toLowerCase(Locale.ENGLISH), new ArrayList<>());
                records.add(encoded);
                accounts.put(account.getUsername().trim().toLowerCase(Locale.ENGLISH), records);
                writeAccountsToJsonRepository(accounts);
                return encoded;
            } catch (final Exception e) {
                LoggingUtils.error(LOGGER, e);
            }
            return null;
        });
    }

    @Override
    public OneTimeTokenAccount update(final OneTimeTokenAccount account) {
        return lock.tryLock(() -> {
            try {
                val accounts = readAccountsFromJsonRepository();
                if (accounts.containsKey(account.getUsername().trim().toLowerCase(Locale.ENGLISH))) {
                    val records = accounts.get(account.getUsername().trim().toLowerCase(Locale.ENGLISH));
                    return records.stream()
                        .filter(rec -> rec.getId() == account.getId())
                        .findFirst()
                        .map(act -> {
                            val encoded = encode(account);
                            act.setSecretKey(encoded.getSecretKey());
                            act.setScratchCodes(encoded.getScratchCodes());
                            act.setValidationCode(encoded.getValidationCode());
                            act.setProperties(encoded.getProperties());
                            writeAccountsToJsonRepository(accounts);
                            return encoded;
                        })
                        .orElse(null);
                }
            } catch (final Exception e) {
                LoggingUtils.error(LOGGER, e);
            }
            return null;
        });
    }

    @Override
    public void deleteAll() {
        lock.tryLock(__ -> writeAccountsToJsonRepository(new HashMap<>(0)));
    }

    @Override
    public void delete(final String username) {
        lock.tryLock(__ -> {
            val accounts = readAccountsFromJsonRepository();
            accounts.remove(username.trim().toLowerCase(Locale.ENGLISH));
            writeAccountsToJsonRepository(accounts);
        });
    }

    @Override
    public void delete(final long id) {
        lock.tryLock(__ -> {
            val accounts = readAccountsFromJsonRepository();
            accounts.forEach((key, value) -> value.removeIf(d -> d.getId() == id));
            writeAccountsToJsonRepository(accounts);
        });
    }

    @Override
    public long count() {
        return lock.tryLock(() -> {
            val accounts = readAccountsFromJsonRepository();
            return accounts.size();
        });
    }

    @Override
    public long count(final String username) {
        return lock.tryLock(() -> {
            val accounts = readAccountsFromJsonRepository();
            return accounts.containsKey(username.trim().toLowerCase(Locale.ENGLISH)) ? accounts.get(username.trim().toLowerCase(Locale.ENGLISH)).size() : 0;
        });
    }

    private void writeAccountsToJsonRepository(final Map<String, List<OneTimeTokenAccount>> accounts) {
        FunctionUtils.doUnchecked(__ -> {
            if (location.getFile() != null) {
                LOGGER.debug("Saving [{}] google authenticator accounts to JSON file at [{}]", accounts.size(), location.getFile());
                serializer.to(location.getFile(), accounts);
            }
        });
    }

    private Map<String, List<OneTimeTokenAccount>> readAccountsFromJsonRepository() {
        return FunctionUtils.doUnchecked(() -> {
            val file = location.getFile();
            LOGGER.debug("Ensuring JSON repository file exists at [{}]", file);
            val result = file != null && file.createNewFile();
            if (result) {
                LOGGER.debug("Created JSON repository file at [{}]", file);
            }
            if (file != null && file.length() > 0) {
                LOGGER.debug("Reading JSON repository file at [{}]", file);
                val accounts = this.serializer.from(file);
                LOGGER.debug("Read [{}] accounts from JSON repository file at [{}]", accounts.size(), file);
                return accounts;
            }
            return new HashMap<>(0);
        });
    }
}
