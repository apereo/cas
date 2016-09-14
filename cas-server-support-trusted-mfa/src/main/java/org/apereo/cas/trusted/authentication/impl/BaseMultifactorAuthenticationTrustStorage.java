package org.apereo.cas.trusted.authentication.impl;

import org.apache.commons.lang.StringUtils;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.trusted.MultifactorAuthenticationTrustUtils;
import org.apereo.cas.trusted.authentication.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.MultifactorAuthenticationTrustStorage;

import java.time.LocalDate;
import java.util.Set;

/**
 * This is {@link BaseMultifactorAuthenticationTrustStorage}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public abstract class BaseMultifactorAuthenticationTrustStorage implements MultifactorAuthenticationTrustStorage {

    /**
     * Number of days records can remain valid.
     */
    protected long numberOfDays;
    
    private CipherExecutor<String, String> cipherExecutor;

    @Override
    public Set<MultifactorAuthenticationTrustRecord> get(final String principal, final LocalDate onOrAfterDate) {
        final Set<MultifactorAuthenticationTrustRecord> res = get(principal);
        res.removeIf(entry -> {
            if (entry.getDate().isBefore(onOrAfterDate)) {
                return true;
            }
            final String decodedKey = this.cipherExecutor.decode(entry.getKey());
            final String currentKey = MultifactorAuthenticationTrustUtils.generateKey(entry);
            if (StringUtils.isBlank(decodedKey)) {
                return true;
            }
            if (!decodedKey.equals(currentKey)) {
                return true;
            }
            return false;
        });
        return res;
    }

    /**
     * Generate key .
     *
     * @param r the record
     * @return the string
     */
    protected String generateKey(final MultifactorAuthenticationTrustRecord r) {
        return cipherExecutor.encode(MultifactorAuthenticationTrustUtils.generateKey(r));
    }

    public void setCipherExecutor(final CipherExecutor<String, String> cipherExecutor) {
        this.cipherExecutor = cipherExecutor;
    }

    public void setNumberOfDays(final long numberOfDays) {
        this.numberOfDays = numberOfDays;
    }
}
