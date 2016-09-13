package org.apereo.cas.trusted.authentication;

import java.time.LocalDate;
import java.util.Set;

/**
 * This is {@link MultifactorAuthenticationTrustStorage}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface MultifactorAuthenticationTrustStorage {
    /**
     * Get record.
     *
     * @param principal the principal id
     * @return the optional
     */
    Set<AuthenticationTrustRecord> get(String principal);

    /**
     * Get record by date.
     *
     * @param principal     the principal id
     * @param onOrAfterDate the on or after date
     * @return the optional
     */
    Set<AuthenticationTrustRecord> get(String principal, LocalDate onOrAfterDate);

    /**
     * Set trusted record.
     *
     * @param record the record
     */
    void set(AuthenticationTrustRecord record);
}
