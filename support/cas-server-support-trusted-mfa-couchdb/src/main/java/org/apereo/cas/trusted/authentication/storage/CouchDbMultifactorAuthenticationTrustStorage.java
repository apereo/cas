package org.apereo.cas.trusted.authentication.storage;

import org.apereo.cas.couchdb.trusted.CouchDbMultifactorAuthenticationTrustRecord;
import org.apereo.cas.couchdb.trusted.MultifactorAuthenticationTrustRecordCouchDbRepository;
import org.apereo.cas.trusted.authentication.api.MultifactorAuthenticationTrustRecord;
import org.apereo.cas.util.CollectionUtils;

import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * This is {@link CouchDbMultifactorAuthenticationTrustStorage}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@AllArgsConstructor
public class CouchDbMultifactorAuthenticationTrustStorage extends BaseMultifactorAuthenticationTrustStorage {

    private MultifactorAuthenticationTrustRecordCouchDbRepository couchDb;

    @Override
    protected MultifactorAuthenticationTrustRecord setInternal(final MultifactorAuthenticationTrustRecord record) {
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
    public void expire(final LocalDateTime onOrBefore) {
        couchDb.findOnOrBeforeDate(onOrBefore).forEach(couchDb::deleteRecord);
    }

    @Override
    public void expire(final String key) {
        couchDb.deleteRecord(couchDb.findByRecordKey(key));
    }

    @Override
    public Set<? extends MultifactorAuthenticationTrustRecord> get(final LocalDateTime onOrAfterDate) {
        return CollectionUtils.wrapHashSet(couchDb.findOnOrAfterDate(onOrAfterDate));
    }

    @Override
    public Set<? extends MultifactorAuthenticationTrustRecord> get(final String principal) {
        return CollectionUtils.wrapHashSet(couchDb.findByPrincipal(principal));
    }

    @Override
    public Set<? extends MultifactorAuthenticationTrustRecord> get(final String principal, final LocalDateTime onOrAfterDate) {
        return CollectionUtils.wrapHashSet(couchDb.findByPrincipalAfterDate(principal, onOrAfterDate));
    }
}
