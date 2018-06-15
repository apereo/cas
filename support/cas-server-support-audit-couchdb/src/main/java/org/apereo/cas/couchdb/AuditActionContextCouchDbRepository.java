package org.apereo.cas.couchdb;

import lombok.Getter;
import org.apereo.inspektr.audit.AuditActionContext;
import org.ektorp.CouchDbConnector;
import org.ektorp.support.CouchDbRepositorySupport;
import org.ektorp.support.View;

import java.time.LocalDate;
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

    @View(name = "by_when_action_was_performed", map = "function(doc) { if(doc.whenActionWasPerformed) { emit(doc.whenActionWasPerformed, doc) } }")
    public List<CouchDbAuditActionContext> findAuditRecordsSince(final LocalDate localDate) {
        return db.queryView(createQuery("by_when_action_was_performed").startKey(localDate).includeDocs(true), CouchDbAuditActionContext.class);
    }
}
