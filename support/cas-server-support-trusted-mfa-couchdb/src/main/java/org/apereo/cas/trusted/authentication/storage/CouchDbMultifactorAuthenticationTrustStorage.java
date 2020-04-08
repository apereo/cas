package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.configuration.model.support.mfa.TrustedDevicesMultifactorProperties;
import org.apereo.cas.couchdb.trusted.CouchDbMultifactorAuthenticationTrustRecord;
import org.apereo.cas.couchdb.trusted.MultifactorAuthenticationTrustRecordCouchDbRepository;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecordKeyGenerator;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.crypto.CipherExecutor;

import lombok.val;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Set;

/**
 * This is {@link CouchDbMultifactorAuthenticationTrustStorage}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
public class CouchDbMultifactorAuthenticationTrustStorage extends BaseMultifactorAuthenticationTrustStorage {

    private final MultifactorAuthenticationTrustRecordCouchDbRepository couchDb;

    public CouchDbMultifactorAuthenticationTrustStorage(final TrustedDevicesMultifactorProperties properties,
                                                        final CipherExecutor<Serializable, String> cipherExecutor,
                                                        final MultifactorAuthenticationTrustRecordCouchDbRepository couchDb,
                                                        final MultifactorAuthenticationTrustRecordKeyGenerator keyGenerationStrategy) {
        super(properties, cipherExecutor, keyGenerationStrategy);
        this.couchDb = couchDb;
    }

    @Override
    protected MultifactorAuthenticationTrustRecord saveInternal(final MultifactorAuthenticationTrustRecord record) {
        var couchDbRecord = couchDb.findByRecordKey(record.getRecordKey());
        if (couchDbRecord == null) {
            couchDbRecord = new CouchDbMultifactorAuthenticationTrustRecord(record);
        } else {
            couchDbRecord.merge(record);
        }

        couchDb.updateRecord(couchDbRecord);
        return couchDbRecord;
    }

    @Override
    public void remove(final ZonedDateTime expirationTime) {
        val records = couchDb.findOnOrAfterExpirationDate(expirationTime);
        records.forEach(couchDb::deleteRecord);
    }
    
    @Override
    public void remove(final String key) {
        couchDb.deleteRecord(couchDb.findByRecordKey(key));
    }

    @Override
    public MultifactorAuthenticationTrustRecord get(final long id) {
        remove();
        return couchDb.findById(id);
    }

    @Override
    public Set<? extends MultifactorAuthenticationTrustRecord> get(final ZonedDateTime onOrAfterDate) {
        remove();
        return CollectionUtils.wrapHashSet(couchDb.findOnOrAfterDate(onOrAfterDate));
    }

    @Override
    public Set<? extends MultifactorAuthenticationTrustRecord> get(final String principal) {
        remove();
        return CollectionUtils.wrapHashSet(couchDb.findByPrincipal(principal));
    }

    @Override
    public Set<? extends MultifactorAuthenticationTrustRecord> get(final String principal, final ZonedDateTime onOrAfterDate) {
        remove();
        return CollectionUtils.wrapHashSet(couchDb.findByPrincipalAfterDate(principal, onOrAfterDate));
    }

    @Override
    public Set<? extends MultifactorAuthenticationTrustRecord> getAll() {
        remove();
        return CollectionUtils.wrapHashSet(couchDb.findAll());
    }
}
