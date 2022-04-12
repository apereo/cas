package org.apereo.cas.couchdb.audit;

import lombok.Getter;
import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.ektorp.ComplexKey;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.View;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

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
     * Find audit records.
     *
     * @param whereClause the where clause
     * @return the list
     */
    @View(name = "by_when_action_was_performed", map = "classpath:by_when_params.js")
    public List<CouchDbAuditActionContext> findAuditRecords(final Map<AuditTrailManager.WhereClauseFields, Object> whereClause) {
        val localDate = ((LocalDate) whereClause.get(AuditTrailManager.WhereClauseFields.DATE))
            .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
        val startKey = new StringBuilder();
        startKey.append(localDate);
        if (whereClause.containsKey(AuditTrailManager.WhereClauseFields.PRINCIPAL)) {
            startKey.append(whereClause.get(AuditTrailManager.WhereClauseFields.PRINCIPAL).toString());
        }
        val query = createQuery("by_when_action_was_performed").startKey(startKey.toString()).includeDocs(true);
        return db.queryView(query, CouchDbAuditActionContext.class);
    }

    /**
     * Find audit records for authentication throttling.
     *
     * @param remoteAddress   remote IP address
     * @param username        username
     * @param failureCode     failure code
     * @param applicationCode application code
     * @param cutoffTime      cut off time
     * @return records for authentication throttling decision
     */
    @View(name = "by_throttle_params", map = "classpath:by_throttle_params.js")
    public List<CouchDbAuditActionContext> findByThrottleParams(final String remoteAddress, final String username, final String failureCode,
                                                                final String applicationCode, final LocalDateTime cutoffTime) {
        val view = createQuery("by_throttle_params")
            .startKey(ComplexKey.of(remoteAddress, username, failureCode, applicationCode, cutoffTime))
            .endKey(ComplexKey.of(remoteAddress, username, failureCode, applicationCode, "999999"));
        return db.queryView(view, CouchDbAuditActionContext.class);
    }
}
