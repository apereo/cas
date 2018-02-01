package org.apereo.cas.trusted.authentication.storage;

import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link InMemoryMultifactorAuthenticationTrustStorage}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@AllArgsConstructor
@Getter
public class InMemoryMultifactorAuthenticationTrustStorage extends BaseMultifactorAuthenticationTrustStorage {
    private final LoadingCache<String, MultifactorAuthenticationTrustRecord> storage;

    @Override
    public void expire(final String key) {
        storage.asMap().keySet().removeIf(k -> k.equalsIgnoreCase(key));
    }

    @Override
    public void expire(final LocalDate onOrBefore) {
        final Set<MultifactorAuthenticationTrustRecord> results = storage.asMap()
                .values()
                .stream()
                .filter(entry -> entry.getRecordDate().isEqual(onOrBefore) || entry.getRecordDate().isBefore(onOrBefore))
                .sorted()
                .distinct()
                .collect(Collectors.toSet());

        LOGGER.info("Found [{}] expired records", results.size());
        if (!results.isEmpty()) {
            results.forEach(entry -> storage.invalidate(entry.getRecordKey()));
            LOGGER.info("Invalidated and removed [{}] expired records", results.size());
        }
    }

    @Override
    public Set<MultifactorAuthenticationTrustRecord> get(final LocalDate onOrAfterDate) {
        expire(onOrAfterDate);
        return storage.asMap()
                .values()
                .stream()
                .filter(entry -> entry.getRecordDate().isEqual(onOrAfterDate) || entry.getRecordDate().isAfter(onOrAfterDate))
                .sorted()
                .distinct()
                .collect(Collectors.toSet());
    }

    @Override
    public Set<MultifactorAuthenticationTrustRecord> get(final String principal) {
        return storage.asMap()
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
        return record;
    }
}
