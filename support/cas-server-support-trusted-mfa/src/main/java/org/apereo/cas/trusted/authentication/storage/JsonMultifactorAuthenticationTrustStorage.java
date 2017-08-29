package org.apereo.cas.trusted.authentication.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.util.ResourceUtils;
import org.hjson.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link JsonMultifactorAuthenticationTrustStorage}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class JsonMultifactorAuthenticationTrustStorage extends BaseMultifactorAuthenticationTrustStorage {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonMultifactorAuthenticationTrustStorage.class);
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();
    
    private final Resource location;
    
    private Map<String, MultifactorAuthenticationTrustRecord> storage;

    public JsonMultifactorAuthenticationTrustStorage(final Resource location) {
        this.location = location;
        readTrustedRecordsFromResource();
    }

    @Override
    public void expire(final String key) {
        storage.keySet().removeIf(k -> k.equalsIgnoreCase(key));
        writeTrustedRecordsToResource();
    }

    @Override
    public void expire(final LocalDate onOrBefore) {
        final Set<MultifactorAuthenticationTrustRecord> results = storage
                .values()
                .stream()
                .filter(entry -> entry.getRecordDate().isEqual(onOrBefore) || entry.getRecordDate().isBefore(onOrBefore))
                .sorted()
                .distinct()
                .collect(Collectors.toSet());

        LOGGER.info("Found [{}] expired records", results.size());
        if (!results.isEmpty()) {
            results.forEach(entry -> storage.remove(entry.getRecordKey()));
            LOGGER.info("Invalidated and removed [{}] expired records", results.size());
            writeTrustedRecordsToResource();
        }
    }

    @Override
    public Set<MultifactorAuthenticationTrustRecord> get(final LocalDate onOrAfterDate) {
        expire(onOrAfterDate);
        return storage
                .values()
                .stream()
                .filter(entry -> entry.getRecordDate().isEqual(onOrAfterDate) || entry.getRecordDate().isAfter(onOrAfterDate))
                .sorted()
                .distinct()
                .collect(Collectors.toSet());
    }

    @Override
    public Set<MultifactorAuthenticationTrustRecord> get(final String principal) {
        return storage
                .values()
                .stream()
                .filter(entry -> entry.getPrincipal().equalsIgnoreCase(principal))
                .sorted()
                .distinct()
                .collect(Collectors.toSet());
    }


    @Override
    public MultifactorAuthenticationTrustRecord setInternal(final MultifactorAuthenticationTrustRecord record) {
        this.storage.put(record.getRecordKey(), record);
        writeTrustedRecordsToResource();
        return record;
    }
    
    private void readTrustedRecordsFromResource() {
        this.storage = new LinkedHashMap<>();
        if (ResourceUtils.doesResourceExist(location)) {
            try (Reader reader = new InputStreamReader(location.getInputStream(), StandardCharsets.UTF_8)) {
                final TypeReference<Map<String, MultifactorAuthenticationTrustRecord>> personList = 
                        new TypeReference<Map<String, MultifactorAuthenticationTrustRecord>>() {
                };
                this.storage = MAPPER.readValue(JsonValue.readHjson(reader).toString(), personList);
            } catch (final Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }
    
    private void writeTrustedRecordsToResource() {
        try {
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(this.location.getFile(), this.storage);
            readTrustedRecordsFromResource();
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
