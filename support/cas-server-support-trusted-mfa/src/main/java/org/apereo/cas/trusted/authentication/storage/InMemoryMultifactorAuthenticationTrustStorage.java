package org.apereo.cas.trusted.authentication.storage;

import com.google.common.cache.LoadingCache;
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
public class InMemoryMultifactorAuthenticationTrustStorage extends BaseMultifactorAuthenticationTrustStorage {

    private LoadingCache<String, MultifactorAuthenticationTrustRecord> storage;

    public InMemoryMultifactorAuthenticationTrustStorage(final LoadingCache<String, MultifactorAuthenticationTrustRecord> st) {
        this.storage = st;
    }

    @Override
    public void expire(final String key) {
        storage.asMap().keySet().removeIf(k -> k.equalsIgnoreCase(key));
    }

    @Override
    public void expire(final LocalDate onOrBefore) {
        final Set<MultifactorAuthenticationTrustRecord> results = storage.asMap()
                .values()
                .stream()
                .filter(entry -> entry.getDate().isEqual(onOrBefore) || entry.getDate().isBefore(onOrBefore))
                .sorted()
                .distinct()
                .collect(Collectors.toSet());

        logger.info("Found {} expired records", results.size());
        if (!results.isEmpty()) {
            results.forEach(entry -> storage.invalidate(entry.getKey()));
            logger.info("Invalidated and removed {} expired records", results.size());
        }
    }

    @Override
    public Set<MultifactorAuthenticationTrustRecord> get(final LocalDate onOrAfterDate) {
        expire(onOrAfterDate);
        return storage.asMap()
                .values()
                .stream()
                .filter(entry -> entry.getDate().isEqual(onOrAfterDate) || entry.getDate().isAfter(onOrAfterDate))
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
        this.storage.put(record.getKey(), record);
        return record;
    }
}
