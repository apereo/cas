package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.configuration.model.support.mfa.TrustedDevicesMultifactorProperties;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecordKeyGenerator;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hjson.JsonValue;
import org.springframework.core.io.Resource;

import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * This is {@link JsonMultifactorAuthenticationTrustStorage}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class JsonMultifactorAuthenticationTrustStorage extends BaseMultifactorAuthenticationTrustStorage {
    private static final int MAP_SIZE = 8;

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private final Resource location;

    private Map<String, MultifactorAuthenticationTrustRecord> storage;

    public JsonMultifactorAuthenticationTrustStorage(final TrustedDevicesMultifactorProperties properties,
                                                     final CipherExecutor<Serializable, String> cipherExecutor,
                                                     final Resource location,
                                                     final MultifactorAuthenticationTrustRecordKeyGenerator keyGenerationStrategy) {
        super(properties, cipherExecutor, keyGenerationStrategy);
        this.location = location;
        readTrustedRecordsFromResource();
    }

    @Override
    public void remove(final String key) {
        storage.keySet().removeIf(k -> k.equalsIgnoreCase(key));
        writeTrustedRecordsToResource();
    }

    @Override
    public void remove(final ZonedDateTime expirationDate) {
        val results = storage
            .values()
            .stream()
            .filter(entry -> entry.getExpirationDate() != null)
            .filter(entry -> {
                val expDate = DateTimeUtils.dateOf(expirationDate);
                return expDate.compareTo(entry.getExpirationDate()) >= 0;
            })
            .sorted()
            .collect(Collectors.toCollection(LinkedHashSet::new));

        LOGGER.info("Found [{}] expired trusted-device records", results.size());
        if (!results.isEmpty()) {
            results.forEach(entry -> storage.remove(entry.getRecordKey()));
            LOGGER.info("Invalidated and removed [{}] expired records", results.size());
            writeTrustedRecordsToResource();
        }
    }

    @Override
    public Set<? extends MultifactorAuthenticationTrustRecord> getAll() {
        remove();
        return new TreeSet<>(storage.values());
    }

    @Override
    public MultifactorAuthenticationTrustRecord get(final long id) {
        remove();
        return storage
            .values()
            .stream()
            .filter(entry -> entry.getId() == id)
            .sorted()
            .findFirst()
            .orElse(null);
    }

    @Override
    public Set<? extends MultifactorAuthenticationTrustRecord> get(final ZonedDateTime onOrAfterDate) {
        remove();
        return storage
            .values()
            .stream()
            .filter(entry -> entry.getRecordDate().isEqual(onOrAfterDate) || entry.getRecordDate().isAfter(onOrAfterDate))
            .sorted()
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Set<? extends MultifactorAuthenticationTrustRecord> get(final String principal) {
        remove();
        return storage
            .values()
            .stream()
            .filter(entry -> entry.getPrincipal().equalsIgnoreCase(principal))
            .sorted()
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }


    @Override
    public MultifactorAuthenticationTrustRecord saveInternal(final MultifactorAuthenticationTrustRecord record) {
        this.storage.put(record.getRecordKey(), record);
        writeTrustedRecordsToResource();
        return record;
    }

    @SneakyThrows
    private void readTrustedRecordsFromResource() {
        this.storage = new LinkedHashMap<>(MAP_SIZE);
        if (ResourceUtils.doesResourceExist(location)) {
            try (val reader = new InputStreamReader(location.getInputStream(), StandardCharsets.UTF_8)) {
                val personList = new TypeReference<Map<String, MultifactorAuthenticationTrustRecord>>() {
                };
                this.storage = MAPPER.readValue(JsonValue.readHjson(reader).toString(), personList);
            }
        }
    }

    @SneakyThrows
    private void writeTrustedRecordsToResource() {
        val file = this.location.getFile();
        val res = file.createNewFile();
        if (res) {
            LOGGER.debug("Created JSON resource @ [{}]", this.location);
        }
        MAPPER.writerWithDefaultPrettyPrinter().writeValue(file, this.storage);
        readTrustedRecordsFromResource();
    }
}
