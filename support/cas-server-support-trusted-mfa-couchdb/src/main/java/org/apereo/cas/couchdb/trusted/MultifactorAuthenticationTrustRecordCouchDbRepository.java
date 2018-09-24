package org.apereo.cas.couchdb.trusted;

import org.apereo.cas.util.CollectionUtils;

import lombok.val;
import org.ektorp.ComplexKey;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.UpdateHandler;
import org.ektorp.support.View;

import java.time.LocalDateTime;
import java.util.List;

/**
 * This is {@link MultifactorAuthenticationTrustRecordCouchDbRepository}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@View(name = "all", map = "function(doc) { if (doc.recordKey && doc.principal && doc.deviceFingerprint && doc.recordDate) { emit(doc._id, doc) } }")
public class MultifactorAuthenticationTrustRecordCouchDbRepository extends CouchDbRepositorySupport<CouchDbMultifactorAuthenticationTrustRecord> {

    public MultifactorAuthenticationTrustRecordCouchDbRepository(final CouchDbConnector db, final boolean createIfNotExists) {
        super(CouchDbMultifactorAuthenticationTrustRecord.class, db, createIfNotExists);
    }

    /**
     * Find by recordKey.
     * @param recordKey record key to search for
     * @return trust records for given input
     */
    @View(name = "by_recordKey", map = "function(doc) { if (doc.principal && doc.deviceFingerprint && doc.recordDate) { emit(doc.recordKey, doc) } }")
    public CouchDbMultifactorAuthenticationTrustRecord findByRecordKey(final String recordKey) {
        return db.queryView(createQuery("by_recordKey").key(recordKey).limit(1), CouchDbMultifactorAuthenticationTrustRecord.class)
            .stream().findFirst().orElse(null);
    }

    /**
     * Find by recordDate on of before date.
     * @param recordDate record key to search for
     * @return trust records for given input
     */
    @View(name = "by_recordDate", map = "function(doc) { if (doc.principal && doc.deviceFingerprint && doc.recordDate) { emit(doc.recordDate, doc) } }")
    public List<CouchDbMultifactorAuthenticationTrustRecord> findOnOrBeforeDate(final LocalDateTime recordDate) {
        return db.queryView(createQuery("by_recordDate").endKey(recordDate), CouchDbMultifactorAuthenticationTrustRecord.class);
    }

    /**
     * Find record created on or after date.
     * @param onOrAfterDate cutoff date
     * @return records created on or after date
     */
    public List<CouchDbMultifactorAuthenticationTrustRecord> findOnOrAfterDate(final LocalDateTime onOrAfterDate) {
        return db.queryView(createQuery("by_recordDate").startKey(onOrAfterDate), CouchDbMultifactorAuthenticationTrustRecord.class);
    }

    /**
     * Find by principal name.
     * @param principal name to search for
     * @return records for given principal
     */
    @View(name = "by_principal",
        map = "function(doc) { if (doc.principal && doc.deviceFingerprint && doc.recordDate) { emit(doc.principal, doc) } }")
    public List<CouchDbMultifactorAuthenticationTrustRecord> findByPrincipal(final String principal) {
        val view = createQuery("by_principal").key(principal);
        return db.queryView(view, CouchDbMultifactorAuthenticationTrustRecord.class);
    }

    /**
     * Find by principal on or after date.
     * @param principal Principal to search for
     * @param onOrAfterDate start date for search
     * @return records for principal after date.
     */
    @View(name = "by_principal_date",
        map = "function(doc) { if (doc.recordKey && doc.principal && doc.deviceFingerprint && doc.recordDate) { emit([doc.principal, doc.recordDate], doc) } }")
    public List<CouchDbMultifactorAuthenticationTrustRecord> findByPrincipalAfterDate(final String principal, final LocalDateTime onOrAfterDate) {
        val view = createQuery("by_principal_date")
            .startKey(ComplexKey.of(principal, onOrAfterDate)).endKey(ComplexKey.of(principal, "999999"));

        return db.queryView(view, CouchDbMultifactorAuthenticationTrustRecord.class);
    }

    /**
     * Delete a record without revision checks.
     * @param record record to be deleted
     */
    @UpdateHandler(name = "delete_record", file = "CouchDbMultifactorAuthenticationTrustRecord_delete.js")
    public void deleteRecord(final CouchDbMultifactorAuthenticationTrustRecord record) {
        db.callUpdateHandler(stdDesignDocumentId, "delete_record", record.getCid(), null);
    }

    /**
     * Update a record without revision checks.
     * @param record record to be updated
     */
    @UpdateHandler(name = "update_record", file = "CouchDbMultifactorAuthenticationTrustRecord_update.js")
    public void updateRecord(final CouchDbMultifactorAuthenticationTrustRecord record) {
        if (record.getCid() == null) {
            add(record);
        } else {
            db.callUpdateHandler(stdDesignDocumentId, "update_record", record.getCid(), CollectionUtils.wrap("doc", record));
        }
    }
}
