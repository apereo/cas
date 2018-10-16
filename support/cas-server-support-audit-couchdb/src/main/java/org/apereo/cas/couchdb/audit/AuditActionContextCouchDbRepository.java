package org.apereo.cas.couchdb.audit;

import lombok.Getter;
import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.ektorp.ComplexKey;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.View;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * This is {@link AuditActionContextCouchDbRepository}. DAO for CouchDb stored {@link AuditActionContext}s.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@Getter
@View(name = "all", map = "function(doc) { if(doc.whenActionWasPerformed) { emit(doc._id, doc) } }")
public class AuditActionContextCouchDbRepository extends CouchDbRepositorySupport<CouchDbAuditActionContext> {

    public AuditActionContextCouchDbRepository(final CouchDbConnector db, final boolean createIfNotExists) {
        super(CouchDbAuditActionContext.class, db, createIfNotExists);
    }

    /**
     * Find audit records since +localDate+.
     * @param localDate Date to search from.
     * @return Audit records from after +localDate+.
     */
    @View(name = "by_when_action_was_performed", map = "function(doc) { if(doc.whenActionWasPerformed) { emit(doc.whenActionWasPerformed, doc) } }")
    public List<CouchDbAuditActionContext> findAuditRecordsSince(final LocalDate localDate) {
        return db.queryView(createQuery("by_when_action_was_performed").startKey(localDate).includeDocs(true), CouchDbAuditActionContext.class);
    }

    /**
     * Find audit records for authentication throttling.
     * @param remoteAddress remote IP address
     * @param username username
     * @param failureCode failure code
     * @param applicationCode application code
     * @param cutoffTime cut off time
     * @return records for authentication throttleing decision
     */
    @View(name = "by_throttle_params", map = "classpath:CouchDbAuditActionContext_by_throttle_params.js")
    public List<CouchDbAuditActionContext> findByThrottleParams(final String remoteAddress, final String username, final String failureCode,
                                                                final String applicationCode, final LocalDateTime cutoffTime) {
        val view = createQuery("by_throttle_params").startKey(ComplexKey.of(remoteAddress, username, failureCode, applicationCode, cutoffTime))
            .endKey(ComplexKey.of(remoteAddress, username, failureCode, applicationCode, "999999"));
        return db.queryView(view, CouchDbAuditActionContext.class);
    }
}
