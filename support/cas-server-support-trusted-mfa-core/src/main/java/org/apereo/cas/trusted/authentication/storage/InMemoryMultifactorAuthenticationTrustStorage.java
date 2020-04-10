package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.configuration.model.support.mfa.TrustedDevicesMultifactorProperties;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecordKeyGenerator;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * This is {@link InMemoryMultifactorAuthenticationTrustStorage}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@Getter
public class InMemoryMultifactorAuthenticationTrustStorage extends BaseMultifactorAuthenticationTrustStorage {
    private final LoadingCache<String, MultifactorAuthenticationTrustRecord> storage;

    public InMemoryMultifactorAuthenticationTrustStorage(final TrustedDevicesMultifactorProperties properties,
                                                         final CipherExecutor<Serializable, String> cipherExecutor,
                                                         final LoadingCache<String, MultifactorAuthenticationTrustRecord> storage,
                                                         final MultifactorAuthenticationTrustRecordKeyGenerator keyGenerationStrategy) {
        super(properties, cipherExecutor, keyGenerationStrategy);
        this.storage = storage;
    }

    @Override
    public void remove(final String key) {
        storage.asMap().keySet().removeIf(k -> k.equalsIgnoreCase(key));
    }

    @Override
    public void remove(final ZonedDateTime expirationDate) {
        val results = storage.asMap()
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
            results.forEach(entry -> storage.invalidate(entry.getRecordKey()));
            LOGGER.info("Invalidated and removed [{}] expired records", results.size());
        }
    }

    @Override
    public Set<? extends MultifactorAuthenticationTrustRecord> get(final ZonedDateTime onOrAfterDate) {
        remove();
        return storage.asMap()
            .values()
            .stream()
            .filter(entry -> entry.getExpirationDate() != null
                && (entry.getRecordDate().isEqual(onOrAfterDate) || entry.getRecordDate().isAfter(onOrAfterDate)))
            .sorted()
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Set<? extends MultifactorAuthenticationTrustRecord> get(final String principal) {
        remove();
        return storage.asMap()
            .values()
            .stream()
            .filter(entry -> entry.getPrincipal().equalsIgnoreCase(principal))
            .sorted()
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public MultifactorAuthenticationTrustRecord get(final long id) {
        remove();
        val result = storage.asMap()
            .values()
            .stream()
            .filter(entry -> entry.getId() == id)
            .sorted()
            .findFirst();

        if (result.isPresent()) {
            val record = result.get();
            LOGGER.trace("Found multifactor authentication trust record [{}]", record);
            return record;
        }
        return null;
    }

    @Override
    public Set<? extends MultifactorAuthenticationTrustRecord> getAll() {
        remove();
        return new TreeSet<>(storage.asMap().values());
    }

    @Override
    public MultifactorAuthenticationTrustRecord saveInternal(final MultifactorAuthenticationTrustRecord record) {
        this.storage.put(record.getRecordKey(), record);
        return record;
    }
}
