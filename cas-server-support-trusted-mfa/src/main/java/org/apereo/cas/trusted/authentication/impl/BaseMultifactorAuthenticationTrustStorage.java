package org.apereo.cas.trusted.authentication.impl;

import org.apache.commons.lang.StringUtils;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.trusted.MultifactorAuthenticationTrustUtils;
import org.apereo.cas.trusted.authentication.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.MultifactorAuthenticationTrustStorage;
import org.apereo.inspektr.audit.annotation.Audit;

import java.time.LocalDate;
import java.util.Set;

/**
 * This is {@link BaseMultifactorAuthenticationTrustStorage}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public abstract class BaseMultifactorAuthenticationTrustStorage implements MultifactorAuthenticationTrustStorage {
    
    private CipherExecutor<String, String> cipherExecutor;

    @Audit(action = "TRUSTED_AUTHENTICATION", actionResolverName = "TRUSTED_AUTHENTICATION_ACTION_RESOLVER",
            resourceResolverName = "TRUSTED_AUTHENTICATION_RESOURCE_RESOLVER")
    @Override
    public MultifactorAuthenticationTrustRecord set(final MultifactorAuthenticationTrustRecord record) {
        return setInternal(record);
    }

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

    /**
     * Set records.
     *
     * @param record the record
     * @return the record
     */
    protected abstract MultifactorAuthenticationTrustRecord setInternal(MultifactorAuthenticationTrustRecord record);
}
