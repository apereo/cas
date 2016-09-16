package org.apereo.cas.trusted.authentication.impl;

import com.google.common.cache.LoadingCache;
import org.apereo.cas.trusted.authentication.MultifactorAuthenticationTrustRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link InMemoryMultifactorAuthenticationTrustStorage}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class InMemoryMultifactorAuthenticationTrustStorage extends BaseMultifactorAuthenticationTrustStorage {
    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryMultifactorAuthenticationTrustStorage.class);
    
    private LoadingCache<String, MultifactorAuthenticationTrustRecord> storage;

    public InMemoryMultifactorAuthenticationTrustStorage(final LoadingCache<String, MultifactorAuthenticationTrustRecord> st) {
        this.storage = st;
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
        LOGGER.debug("Stored authentication trust record for {}", record);
        record.setKey(generateKey(record));
        this.storage.put(record.getKey(), record);
        return record;
    }
}
