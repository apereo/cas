package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.audit.AuditActionResolvers;
import org.apereo.cas.audit.AuditResourceResolvers;
import org.apereo.cas.audit.AuditableActions;
import org.apereo.cas.configuration.model.support.mfa.trusteddevice.TrustedDevicesMultifactorProperties;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecordKeyGenerator;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustStorage;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Set;

/**
 * This is {@link BaseMultifactorAuthenticationTrustStorage}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@EnableTransactionManagement
@Transactional(transactionManager = "transactionManagerMfaAuthnTrust")
@Slf4j
@ToString
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public abstract class BaseMultifactorAuthenticationTrustStorage implements MultifactorAuthenticationTrustStorage {
    private final TrustedDevicesMultifactorProperties trustedDevicesMultifactorProperties;

    private final CipherExecutor<Serializable, String> cipherExecutor;

    private final MultifactorAuthenticationTrustRecordKeyGenerator keyGenerationStrategy;

    @Audit(action = AuditableActions.TRUSTED_AUTHENTICATION,
        actionResolverName = AuditActionResolvers.TRUSTED_AUTHENTICATION_ACTION_RESOLVER,
        resourceResolverName = AuditResourceResolvers.TRUSTED_AUTHENTICATION_RESOURCE_RESOLVER)
    @Override
    public MultifactorAuthenticationTrustRecord save(final MultifactorAuthenticationTrustRecord record) {
        if (StringUtils.isBlank(record.getRecordKey())) {
            LOGGER.trace("Generating record key for record [{}]", record.getId());
            record.setRecordKey(generateKey(record));
        }
        LOGGER.debug("Storing authentication trust record for [{}]", record);
        return saveInternal(record);
    }

    @Override
    public Set<? extends MultifactorAuthenticationTrustRecord> get(final String principal, final ZonedDateTime onOrAfterDate) {
        val res = get(principal);
        res.removeIf(entry -> {
            if (entry.getRecordDate().isBefore(onOrAfterDate)) {
                return true;
            }
            val decodedKey = this.cipherExecutor.decode(entry.getRecordKey());
            val currentKey = keyGenerationStrategy.generate(entry);
            return StringUtils.isBlank(decodedKey) || !decodedKey.equals(currentKey);
        });
        return res;
    }

    /**
     * Generate key.
     *
     * @param r the record
     * @return the string
     */
    protected String generateKey(final MultifactorAuthenticationTrustRecord r) {
        val key = keyGenerationStrategy.generate(r);
        return cipherExecutor.encode(key);
    }

    /**
     * Set records.
     *
     * @param record the record
     * @return the record
     */
    protected abstract MultifactorAuthenticationTrustRecord saveInternal(MultifactorAuthenticationTrustRecord record);
}
