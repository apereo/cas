package org.apereo.cas.trusted.authentication.api;

import org.apereo.cas.util.crypto.CipherExecutor;
import java.io.Serializable;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;

/**
 * This is {@link MultifactorAuthenticationTrustStorage}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public interface MultifactorAuthenticationTrustStorage {
    /**
     * Bean name.
     */
    String BEAN_NAME = "mfaTrustEngine";

    /**
     * Remove records that are expired by date.
     *
     * @param expirationTime the expiration time
     */
    void remove(ZonedDateTime expirationTime);

    /**
     * Remove records that are expired by now.
     */
    default void remove() {
        remove(ZonedDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.SECONDS));
    }

    /**
     * Expire device by registration key.
     *
     * @param key the key
     */
    void remove(String key);

    /**
     * Gets all.
     *
     * @return the all
     */
    Set<? extends MultifactorAuthenticationTrustRecord> getAll();

    /**
     * Get all records by date.
     *
     * @param onOrAfterDate the on or after date
     * @return the records
     */
    Set<? extends MultifactorAuthenticationTrustRecord> get(ZonedDateTime onOrAfterDate);

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
    Set<? extends MultifactorAuthenticationTrustRecord> get(String principal, ZonedDateTime onOrAfterDate);

    /**
     * Set trusted record.
     *
     * @param record the record
     * @return the multifactor authentication trust record
     */
    MultifactorAuthenticationTrustRecord save(MultifactorAuthenticationTrustRecord record);

    /**
     * Gets cipher executor.
     *
     * @return the cipher executor
     */
    CipherExecutor<Serializable, String> getCipherExecutor();

    /**
     * Determine if the storage service is available
     * and is able to establish a connection.
     *
     * @return true or false
     */
    default boolean isAvailable() {
        return true;
    }
}
