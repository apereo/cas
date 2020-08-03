package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.configuration.model.support.mfa.TrustedDevicesMultifactorProperties;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecordKeyGenerator;
import org.apereo.cas.util.crypto.CipherExecutor;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Set;

/**
 * This is {@link DynamoDbMultifactorAuthenticationTrustStorage}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class DynamoDbMultifactorAuthenticationTrustStorage extends BaseMultifactorAuthenticationTrustStorage {
    private final DynamoDbMultifactorTrustEngineFacilitator dynamoDbFacilitator;

    public DynamoDbMultifactorAuthenticationTrustStorage(final TrustedDevicesMultifactorProperties properties,
                                                         final CipherExecutor<Serializable, String> cipherExecutor,
                                                         final DynamoDbMultifactorTrustEngineFacilitator dynamoDbFacilitator,
                                                         final MultifactorAuthenticationTrustRecordKeyGenerator keyGenerationStrategy) {
        super(properties, cipherExecutor, keyGenerationStrategy);
        this.dynamoDbFacilitator = dynamoDbFacilitator;
    }

    @Override
    protected MultifactorAuthenticationTrustRecord saveInternal(final MultifactorAuthenticationTrustRecord record) {
        dynamoDbFacilitator.save(record);
        return record;
    }

    @Override
    public void remove(final ZonedDateTime expirationTime) {
        dynamoDbFacilitator.remove(expirationTime);
    }

    @Override
    public void remove(final String key) {
        dynamoDbFacilitator.remove(key);
    }

    @Override
    public Set<? extends MultifactorAuthenticationTrustRecord> get(final ZonedDateTime onOrAfterDate) {
        remove();
        return dynamoDbFacilitator.getRecordForDate(onOrAfterDate);
    }

    @Override
    public Set<? extends MultifactorAuthenticationTrustRecord> get(final String principal) {
        remove();
        return dynamoDbFacilitator.getRecordForPrincipal(principal);
    }

    @Override
    public MultifactorAuthenticationTrustRecord get(final long id) {
        remove();
        return dynamoDbFacilitator.getRecordForId(id);
    }

    @Override
    public Set<? extends MultifactorAuthenticationTrustRecord> getAll() {
        remove();
        return dynamoDbFacilitator.getAll();
    }
}
