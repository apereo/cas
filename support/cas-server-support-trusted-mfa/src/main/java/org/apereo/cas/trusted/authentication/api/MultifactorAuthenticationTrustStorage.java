package org.apereo.cas.trusted.authentication.api;

import java.time.LocalDate;
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
    void expire(LocalDate onOrBefore);

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
    Set<MultifactorAuthenticationTrustRecord> get(LocalDate onOrAfterDate);

    /**
     * Get record.
     *
     * @param principal the principal id
     * @return the records
     */
    Set<MultifactorAuthenticationTrustRecord> get(String principal);

    /**
     * Get record by date.
     *
     * @param principal     the principal id
     * @param onOrAfterDate the on or after date
     * @return the optional
     */
    Set<MultifactorAuthenticationTrustRecord> get(String principal, LocalDate onOrAfterDate);

    /**
     * Set trusted record.
     *
     * @param record the record
     * @return the multifactor authentication trust record
     */
    MultifactorAuthenticationTrustRecord set(MultifactorAuthenticationTrustRecord record);
}
