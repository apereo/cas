package org.apereo.cas.trusted.authentication.storage;

import org.apache.commons.lang.StringUtils;
import org.apereo.cas.CipherExecutor;
import org.apereo.cas.trusted.util.MultifactorAuthenticationTrustUtils;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.inspektr.audit.annotation.Audit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

/**
 * This is {@link BaseMultifactorAuthenticationTrustStorage}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableTransactionManagement(proxyTargetClass = true)
@Transactional(readOnly = false, transactionManager = "transactionManagerMfaAuthnTrust")
public abstract class BaseMultifactorAuthenticationTrustStorage implements MultifactorAuthenticationTrustStorage {
    protected final transient Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private CipherExecutor<String, String> cipherExecutor;

    @Audit(action = "TRUSTED_AUTHENTICATION", actionResolverName = "TRUSTED_AUTHENTICATION_ACTION_RESOLVER",
            resourceResolverName = "TRUSTED_AUTHENTICATION_RESOURCE_RESOLVER")
    @Override
    public MultifactorAuthenticationTrustRecord set(final MultifactorAuthenticationTrustRecord record) {
        logger.debug("Stored authentication trust record for {}", record);
        record.setKey(generateKey(record));
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

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    /**
     * Set records.
     *
     * @param record the record
     * @return the record
     */
    protected abstract MultifactorAuthenticationTrustRecord setInternal(MultifactorAuthenticationTrustRecord record);
}
