package org.apereo.cas.trusted.authentication.api;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

/**
 * This is {@link MultifactorAuthenticationTrustRecordKeyGenerator}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@FunctionalInterface
public interface MultifactorAuthenticationTrustRecordKeyGenerator {
    /**
     * Generate.
     *
     * @param record the record
     * @return the string
     */
    String generate(MultifactorAuthenticationTrustRecord record);

    /**
     * Gets name.
     *
     * @return the name
     */
    default String getName() {
        return getClass().getSimpleName();
    }

    /**
     * Gets principal from record key.
     *
     * @param key the key
     * @return the principal from record key
     */
    default String getPrincipalFromRecordKey(final String key) {
        return Iterables.get(Splitter.on('@').split(key), 0);
    }
}
