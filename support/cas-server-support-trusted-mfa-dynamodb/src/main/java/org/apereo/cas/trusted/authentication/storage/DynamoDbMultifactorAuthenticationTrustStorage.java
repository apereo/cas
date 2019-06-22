package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * This is {@link DynamoDbMultifactorAuthenticationTrustStorage}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class DynamoDbMultifactorAuthenticationTrustStorage extends BaseMultifactorAuthenticationTrustStorage {
    private final DynamoDbMultifactorTrustEngineFacilitator dynamoDbFacilitator;


    @Override
    protected MultifactorAuthenticationTrustRecord setInternal(final MultifactorAuthenticationTrustRecord record) {
        dynamoDbFacilitator.save(record);
        return record;
    }

    @Override
    public void expire(final LocalDateTime onOrBefore) {
        dynamoDbFacilitator.remove(onOrBefore);
    }

    @Override
    public void expire(final String key) {
        dynamoDbFacilitator.remove(key);
    }

    @Override
    public Set<? extends MultifactorAuthenticationTrustRecord> get(final LocalDateTime onOrAfterDate) {
        return dynamoDbFacilitator.getRecordForDate(onOrAfterDate);
    }

    @Override
    public Set<? extends MultifactorAuthenticationTrustRecord> get(final String principal) {
        return dynamoDbFacilitator.getRecordForPrincipal(principal);
    }

    @Override
    public MultifactorAuthenticationTrustRecord get(final long id) {
        return dynamoDbFacilitator.getRecordForId(id);
    }
}
