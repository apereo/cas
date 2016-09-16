package org.apereo.cas.trusted.authentication.storage;

import com.google.common.cache.LoadingCache;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
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
        this.storage.put(record.getKey(), record);
        return record;
    }
}
