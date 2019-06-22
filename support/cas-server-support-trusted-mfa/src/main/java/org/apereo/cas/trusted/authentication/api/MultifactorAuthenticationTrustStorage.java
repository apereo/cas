package org.apereo.cas.trusted.authentication.api;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * This is {@link MultifactorAuthenticationTrustStorage}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface MultifactorAuthenticationTrustStorage {

    /**
     * Expire records that are on/before the provided date.
     *
     * @param onOrBefore the on or before
     */
    void expire(LocalDateTime onOrBefore);

    /**
     * Expire device by registration key.
     *
     * @param key the key
     */
    void expire(String key);

    /**
     * Get all records by date.
     *
     * @param onOrAfterDate the on or after date
     * @return the records
     */
    Set<? extends MultifactorAuthenticationTrustRecord> get(LocalDateTime onOrAfterDate);

    /**
     * Get record.
     *
     * @param principal the principal id
     * @return the records
     */
    Set<? extends MultifactorAuthenticationTrustRecord> get(String principal);

    /**
     * Get record.
     *
     * @param id the id
     * @return the records
     */
    MultifactorAuthenticationTrustRecord get(long id);

    /**
     * Get record by date.
     *
     * @param principal     the principal id
     * @param onOrAfterDate the on or after date
     * @return the optional
     */
    Set<? extends MultifactorAuthenticationTrustRecord> get(String principal, LocalDateTime onOrAfterDate);

    /**
     * Set trusted record.
     *
     * @param record the record
     * @return the multifactor authentication trust record
     */
    MultifactorAuthenticationTrustRecord set(MultifactorAuthenticationTrustRecord record);
}
