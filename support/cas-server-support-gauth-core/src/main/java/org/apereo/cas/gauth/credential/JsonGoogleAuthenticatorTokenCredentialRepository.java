package org.apereo.cas.gauth.credential;

import module java.base;
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
            .orElse(null));
    }

    /**
     * Fetch the accounts registered for the user. A missing file, an empty file or an empty JSON object means no
     * accounts; a file that cannot be read or parsed, or a lock that cannot be acquired, is raised as an error instead,
     * because an empty result sends the user to device registration.
     *
     * @param username the username
     * @return the accounts registered for the user
     */
    @Override
    public Collection<? extends OneTimeTokenAccount> get(final String username) {
        val accounts = lock.<Collection<? extends OneTimeTokenAccount>>tryLock(() -> {
            if (!location.exists()) {
                LOGGER.warn("JSON account repository [{}] is not found.", location);
                return new ArrayList<>();
            }
            val file = location.getFile();
            val content = Files.readString(file.toPath(), StandardCharsets.UTF_8).trim();
            if (content.isEmpty() || "{}".equals(content)) {
                LOGGER.debug("JSON account repository file location [{}] is empty.", file);
                return new ArrayList<>();
            }
            val map = Objects.requireNonNull(serializer.from(file),
                () -> "Unable to parse JSON account repository file %s".formatted(file));
            val account = map.get(username.trim().toLowerCase(Locale.ENGLISH));
            return account != null ? decode(account) : new ArrayList<>();
        });
        return Objects.requireNonNull(accounts, () -> "Unable to read accounts for %s from %s".formatted(username, location));
    }

    @Override
    public Collection<? extends OneTimeTokenAccount> load() {
        return lock.tryLock(() -> {
            try {
                return readAccountsFromJsonRepository()
                    .values()
                    .stream()
                    .flatMap(List::stream)
                    .map(this::decode)
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
                records.removeIf(rec -> rec.getId() == encoded.getId());
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

    /**
     * Update the stored account with the same id, or add the account when none is stored,
     * so that every backend treats an update of a missing device as an insert.
     *
     * @param account the account
     * @return the encoded account as stored, or null if the repository could not be written
     */
    @Override
    public OneTimeTokenAccount update(final OneTimeTokenAccount account) {
        return lock.tryLock(() -> {
            try {
                val accounts = readAccountsFromJsonRepository();
                val encoded = encode(account);
                val records = accounts.computeIfAbsent(encoded.getUsername(), _ -> new ArrayList<>());
                records.stream()
                    .filter(rec -> rec.getId() == encoded.getId())
                    .findFirst()
                    .ifPresentOrElse(act -> {
                        act.setSecretKey(encoded.getSecretKey());
                        act.setScratchCodes(encoded.getScratchCodes());
                        act.setValidationCode(encoded.getValidationCode());
                        act.setProperties(encoded.getProperties());
                        act.setLastUsedDateTime(encoded.getLastUsedDateTime());
                    }, () -> records.add(encoded));
                writeAccountsToJsonRepository(accounts);
                return encoded;
            } catch (final Exception e) {
                LoggingUtils.error(LOGGER, e);
            }
            return null;
        });
    }

    @Override
    public void deleteAll() {
        lock.tryLock(_ -> writeAccountsToJsonRepository(new HashMap<>()));
    }

    @Override
    public void delete(final String username) {
        lock.tryLock(_ -> {
            val accounts = readAccountsFromJsonRepository();
            accounts.remove(username.trim().toLowerCase(Locale.ENGLISH));
            writeAccountsToJsonRepository(accounts);
        });
    }

    @Override
    public void delete(final long id) {
        lock.tryLock(_ -> {
            val accounts = readAccountsFromJsonRepository();
            accounts.forEach((key, value) -> value.removeIf(d -> d.getId() == id));
            writeAccountsToJsonRepository(accounts);
        });
    }

    @Override
    public long count() {
        return lock.tryLock(() -> {
            val accounts = readAccountsFromJsonRepository();
            return accounts.values().stream().mapToLong(List::size).sum();
        });
    }

    @Override
    public long count(final String username) {
        return lock.tryLock(() -> {
            val accounts = readAccountsFromJsonRepository();
            return accounts.containsKey(username.trim().toLowerCase(Locale.ENGLISH)) ? accounts.get(username.trim().toLowerCase(Locale.ENGLISH)).size() : 0;
        });
    }

    /**
     * Write the accounts to a temporary file next to the repository file and move it over the repository file
     * in one atomic step, so that a failed or interrupted write never leaves a truncated file behind.
     * The lock only covers this JVM; the JSON repository is not meant to be shared by several CAS nodes.
     *
     * @param accounts the accounts to write
     */
    private void writeAccountsToJsonRepository(final Map<String, List<OneTimeTokenAccount>> accounts) {
        FunctionUtils.doUnchecked(_ -> {
            val file = location.getFile();
            if (file != null) {
                val path = file.toPath();
                val target = Files.exists(path) ? path.toRealPath() : path.toAbsolutePath();
                LOGGER.debug("Saving [{}] google authenticator accounts to JSON file at [{}]", accounts.size(), target);
                val temp = Files.createTempFile(target.getParent(), target.getFileName().toString(), ".tmp");
                try {
                    serializer.to(temp.toFile(), accounts);
                    Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                } finally {
                    Files.deleteIfExists(temp);
                }
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
            return new HashMap<>();
        });
    }
}
