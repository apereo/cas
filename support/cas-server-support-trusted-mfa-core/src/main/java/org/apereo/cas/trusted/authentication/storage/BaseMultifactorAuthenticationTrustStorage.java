package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.trusted.util.MultifactorAuthenticationTrustUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * This is {@link BaseMultifactorAuthenticationTrustStorage}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableTransactionManagement(proxyTargetClass = true)
@Transactional(transactionManager = "transactionManagerMfaAuthnTrust")
@Slf4j
@ToString
@Setter
public abstract class BaseMultifactorAuthenticationTrustStorage implements MultifactorAuthenticationTrustStorage {

    private CipherExecutor<Serializable, String> cipherExecutor;

    @Audit(action = "TRUSTED_AUTHENTICATION",
        actionResolverName = "TRUSTED_AUTHENTICATION_ACTION_RESOLVER",
        resourceResolverName = "TRUSTED_AUTHENTICATION_RESOURCE_RESOLVER")
    @Override
    public MultifactorAuthenticationTrustRecord set(final MultifactorAuthenticationTrustRecord record) {
        LOGGER.debug("Stored authentication trust record for [{}]", record);
        record.setRecordKey(generateKey(record));
        return setInternal(record);
    }

    @Override
    public Set<? extends MultifactorAuthenticationTrustRecord> get(final String principal, final LocalDateTime onOrAfterDate) {
        val res = get(principal);
        res.removeIf(entry -> {
            if (entry.getRecordDate().isBefore(onOrAfterDate)) {
                return true;
            }
            val decodedKey = this.cipherExecutor.decode(entry.getRecordKey());
            val currentKey = MultifactorAuthenticationTrustUtils.generateKey(entry);
            return StringUtils.isBlank(decodedKey) || !decodedKey.equals(currentKey);
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

    /**
     * Set records.
     *
     * @param record the record
     * @return the record
     */
    protected abstract MultifactorAuthenticationTrustRecord setInternal(MultifactorAuthenticationTrustRecord record);
}
