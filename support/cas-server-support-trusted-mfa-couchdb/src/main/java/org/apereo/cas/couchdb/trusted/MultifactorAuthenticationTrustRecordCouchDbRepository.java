package org.apereo.cas.couchdb.trusted;

import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.DateTimeUtils;

import lombok.val;
import org.ektorp.ComplexKey;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.UpdateHandler;
import org.ektorp.support.View;

import java.time.ZonedDateTime;
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
     *
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
     *
     * @param recordDate record key to search for
     * @return trust records for given input
     */
    @View(name = "by_recordDate", map = "function(doc) { if (doc.principal && doc.deviceFingerprint && doc.recordDate) { emit(doc.recordDate, doc) } }")
    public List<CouchDbMultifactorAuthenticationTrustRecord> findOnOrBeforeDate(final ZonedDateTime recordDate) {
        return db.queryView(createQuery("by_recordDate").endKey(recordDate), CouchDbMultifactorAuthenticationTrustRecord.class);
    }

    /**
     * Find record created on or after date.
     *
     * @param onOrAfterDate cutoff date
     * @return records created on or after date
     */
    public List<CouchDbMultifactorAuthenticationTrustRecord> findOnOrAfterDate(final ZonedDateTime onOrAfterDate) {
        return db.queryView(createQuery("by_recordDate").startKey(onOrAfterDate), CouchDbMultifactorAuthenticationTrustRecord.class);
    }

    /**
     * Find record created on or after exp date.
     * Remove all records whose expiration date is greater than the given date.
     * @param onOrAfterDate cutoff date
     * @return records created on or after date
     */
    @View(name = "by_expirationDate", map = "function(doc) { if (doc.principal && doc.deviceFingerprint && doc.expirationDate) { emit(doc.expirationDate, doc) } }")
    public List<CouchDbMultifactorAuthenticationTrustRecord> findOnOrAfterExpirationDate(final ZonedDateTime onOrAfterDate) {
        val expDate = DateTimeUtils.dateOf(onOrAfterDate);
        val query = createQuery("by_expirationDate").endKey(expDate);
        return db.queryView(query, CouchDbMultifactorAuthenticationTrustRecord.class);
    }

    /**
     * Find by principal name.
     *
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
     * Find by id.
     *
     * @param id the id
     * @return the list
     */
    @View(name = "by_id",
        map = "function(doc) { if (doc.principal && doc.deviceFingerprint && doc.recordDate) { emit(doc.id, doc) } }")
    public CouchDbMultifactorAuthenticationTrustRecord findById(final long id) {
        val view = createQuery("by_id").key(id);
        return db.queryView(view, CouchDbMultifactorAuthenticationTrustRecord.class)
            .stream()
            .findFirst()
            .orElse(null);
    }

    /**
     * Find by principal on or after date.
     *
     * @param principal     Principal to search for
     * @param onOrAfterDate start date for search
     * @return records for principal after date.
     */
    @View(name = "by_principal_date",
        map = "function(doc) { if (doc.recordKey && doc.principal && doc.deviceFingerprint && doc.recordDate) { emit([doc.principal, doc.recordDate], doc) } }")
    public List<CouchDbMultifactorAuthenticationTrustRecord> findByPrincipalAfterDate(final String principal, final ZonedDateTime onOrAfterDate) {
        val view = createQuery("by_principal_date")
            .startKey(ComplexKey.of(principal, onOrAfterDate))
            .endKey(ComplexKey.of(principal, String.valueOf(Long.MAX_VALUE)));

        return db.queryView(view, CouchDbMultifactorAuthenticationTrustRecord.class);
    }

    /**
     * Find all list.
     *
     * @return the list
     */
    @View(name = "by_all", map = "function(doc) { if (doc.recordKey) { emit([doc.recordKey], doc) } }")
    public List<CouchDbMultifactorAuthenticationTrustRecord> findAll() {
        val view = createQuery("by_all");
        return db.queryView(view, CouchDbMultifactorAuthenticationTrustRecord.class);
    }

    /**
     * Delete a record without revision checks.
     *
     * @param record record to be deleted
     */
    @UpdateHandler(name = "delete_record", file = "CouchDbMultifactorAuthenticationTrustRecord_delete.js")
    public void deleteRecord(final CouchDbMultifactorAuthenticationTrustRecord record) {
        db.callUpdateHandler(stdDesignDocumentId, "delete_record", record.getCid(), null);
    }

    /**
     * Update a record without revision checks.
     *
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
