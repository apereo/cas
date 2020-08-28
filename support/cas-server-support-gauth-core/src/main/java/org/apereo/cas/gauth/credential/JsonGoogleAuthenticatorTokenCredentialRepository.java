package org.apereo.cas.gauth.credential;

import org.apereo.cas.authentication.OneTimeTokenAccount;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.util.crypto.CipherExecutor;
import org.apereo.cas.util.serialization.AbstractJacksonBackedStringSerializer;
import org.apereo.cas.util.serialization.StringSerializer;

import com.warrenstrange.googleauth.IGoogleAuthenticator;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
    private final Resource location;

    private final StringSerializer<Map<String, List<OneTimeTokenAccount>>> serializer = new OneTimeAccountSerializer();

    public JsonGoogleAuthenticatorTokenCredentialRepository(final Resource location, final IGoogleAuthenticator googleAuthenticator,
                                                            final CipherExecutor<String, String> tokenCredentialCipher) {
        super(tokenCredentialCipher, googleAuthenticator);
        this.location = location;
    }

    @Override
    public OneTimeTokenAccount get(final String username, final long id) {
        return get(username).stream().filter(ac -> ac.getId() == id).findFirst().orElse(null);
    }

    @Override
    public OneTimeTokenAccount get(final long id) {
        try {
            val accounts = readAccountsFromJsonRepository();
            return accounts.values()
                .stream()
                .flatMap(List::stream)
                .filter(ac -> ac.getId() == id)
                .findFirst()
                .orElse(null);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return null;
    }

    @Override
    public Collection<? extends OneTimeTokenAccount> get(final String username) {
        try {
            if (!location.getFile().exists()) {
                LOGGER.warn("JSON account repository file [{}] is not found.", location.getFile());
                return new ArrayList<>(0);
            }

            if (location.getFile().length() <= 0) {
                LOGGER.warn("JSON account repository file location [{}] is empty.", location.getFile());
                return new ArrayList<>(0);
            }
            val map = this.serializer.from(location.getFile());
            if (map == null) {
                LOGGER.debug("JSON account repository file [{}] is empty.", location.getFile());
                return new ArrayList<>(0);
            }

            val account = map.get(username);
            if (account != null) {
                return decode(account);
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return new ArrayList<>(0);
    }

    @Override
    public OneTimeTokenAccount save(final OneTimeTokenAccount account) {
        try {
            LOGGER.debug("Storing google authenticator account for [{}]", account.getUsername());
            val accounts = readAccountsFromJsonRepository();
            LOGGER.debug("Found [{}] account(s) and added google authenticator account for [{}]",
                accounts.size(), account.getUsername());
            val encoded = encode(account);
            val records = accounts.getOrDefault(account.getUsername(), new ArrayList<>());
            records.add(encoded);
            accounts.put(account.getUsername(), records);
            writeAccountsToJsonRepository(accounts);
            return encoded;
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return null;
    }

    @Override
    public OneTimeTokenAccount update(final OneTimeTokenAccount account) {
        try {
            val accounts = readAccountsFromJsonRepository();
            if (accounts.containsKey(account.getUsername())) {
                val records = accounts.get(account.getUsername());
                return records.stream()
                    .filter(rec -> rec.getId() == account.getId())
                    .findFirst()
                    .map(act -> {
                        val encoded = encode(account);
                        act.setSecretKey(encoded.getSecretKey());
                        act.setScratchCodes(encoded.getScratchCodes());
                        act.setValidationCode(encoded.getValidationCode());
                        writeAccountsToJsonRepository(accounts);
                        return encoded;
                    })
                    .orElse(null);
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return null;
    }

    @Override
    public void deleteAll() {
        writeAccountsToJsonRepository(new HashMap<>(0));
    }

    @Override
    public void delete(final String username) {
        try {
            val accounts = readAccountsFromJsonRepository();
            accounts.remove(username);
            writeAccountsToJsonRepository(accounts);
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
    }

    @Override
    public long count() {
        try {
            val accounts = readAccountsFromJsonRepository();
            return accounts.size();
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return 0;
    }

    @Override
    public long count(final String username) {
        try {
            val accounts = readAccountsFromJsonRepository();
            return accounts.get(username).size();
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return 0;
    }

    @Override
    public Collection<? extends OneTimeTokenAccount> load() {
        try {
            return readAccountsFromJsonRepository().values()
                .stream().flatMap(List::stream).collect(Collectors.toList());
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        return new ArrayList<>(0);
    }

    @SneakyThrows
    private void writeAccountsToJsonRepository(final Map<String, List<OneTimeTokenAccount>> accounts) {
        LOGGER.debug("Saving [{}] google authenticator accounts to JSON file at [{}]", accounts.size(), location.getFile());
        this.serializer.to(location.getFile(), accounts);
    }

    private Map<String, List<OneTimeTokenAccount>> readAccountsFromJsonRepository() throws IOException {
        LOGGER.debug("Ensuring JSON repository file exists at [{}]", location.getFile());
        val result = location.getFile().createNewFile();
        if (result) {
            LOGGER.debug("Created JSON repository file at [{}]", location.getFile());
        }
        if (location.getFile().length() > 0) {
            LOGGER.debug("Reading JSON repository file at [{}]", location.getFile());
            val accounts = this.serializer.from(location.getFile());
            LOGGER.debug("Read [{}] accounts from JSON repository file at [{}]", accounts.size(), location.getFile());
            return accounts;
        }
        return new HashMap<>(0);
    }

    private static class OneTimeAccountSerializer extends AbstractJacksonBackedStringSerializer<Map<String, List<OneTimeTokenAccount>>> {
        private static final long serialVersionUID = 1466569521275630254L;

        @Override
        public Class getTypeToSerialize() {
            return HashMap.class;
        }
    }
}
