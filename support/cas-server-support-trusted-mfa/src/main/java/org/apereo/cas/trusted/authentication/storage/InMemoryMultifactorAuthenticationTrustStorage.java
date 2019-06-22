package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;

import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link InMemoryMultifactorAuthenticationTrustStorage}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@RequiredArgsConstructor
@Getter
public class InMemoryMultifactorAuthenticationTrustStorage extends BaseMultifactorAuthenticationTrustStorage {
    private final LoadingCache<String, MultifactorAuthenticationTrustRecord> storage;

    @Override
    public void expire(final String key) {
        storage.asMap().keySet().removeIf(k -> k.equalsIgnoreCase(key));
    }

    @Override
    public void expire(final LocalDateTime onOrBefore) {
        val results = storage.asMap()
            .values()
            .stream()
            .filter(entry -> entry.getRecordDate().isEqual(onOrBefore) || entry.getRecordDate().isBefore(onOrBefore))
            .sorted()
            .collect(Collectors.toCollection(LinkedHashSet::new));

        LOGGER.info("Found [{}] expired trusted-device records", results.size());
        if (!results.isEmpty()) {
            results.forEach(entry -> storage.invalidate(entry.getRecordKey()));
            LOGGER.info("Invalidated and removed [{}] expired records", results.size());
        }
    }

    @Override
    public Set<? extends MultifactorAuthenticationTrustRecord> get(final LocalDateTime onOrAfterDate) {
        expire(onOrAfterDate);
        return storage.asMap()
            .values()
            .stream()
            .filter(entry -> entry.getRecordDate().isEqual(onOrAfterDate) || entry.getRecordDate().isAfter(onOrAfterDate))
            .sorted()
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Set<? extends MultifactorAuthenticationTrustRecord> get(final String principal) {
        return storage.asMap()
            .values()
            .stream()
            .filter(entry -> entry.getPrincipal().equalsIgnoreCase(principal))
            .sorted()
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public MultifactorAuthenticationTrustRecord get(final long id) {
        return storage.asMap()
            .values()
            .stream()
            .filter(entry -> entry.getId() == id)
            .sorted()
            .findFirst()
            .orElse(null);
    }

    @Override
    public MultifactorAuthenticationTrustRecord setInternal(final MultifactorAuthenticationTrustRecord record) {
        this.storage.put(record.getRecordKey(), record);
        return record;
    }
}
