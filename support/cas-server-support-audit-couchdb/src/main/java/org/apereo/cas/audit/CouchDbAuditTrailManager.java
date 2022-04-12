package org.apereo.cas.audit;

import org.apereo.cas.audit.spi.AbstractAuditTrailManager;
import org.apereo.cas.couchdb.audit.AuditActionContextCouchDbRepository;
import org.apereo.cas.couchdb.audit.CouchDbAuditActionContext;
import org.apereo.cas.util.CollectionUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apereo.inspektr.audit.AuditActionContext;

import java.util.Map;
import java.util.Set;

/**
 * This is {@link CouchDbAuditTrailManager}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@AllArgsConstructor
@Getter
@Setter
public class CouchDbAuditTrailManager extends AbstractAuditTrailManager {
    private final AuditActionContextCouchDbRepository couchDb;

    public CouchDbAuditTrailManager(final boolean asynchronous, final AuditActionContextCouchDbRepository couchDb) {
        super(asynchronous);
        this.couchDb = couchDb;
    }

    @Override
    protected void saveAuditRecord(final AuditActionContext audit) {
        couchDb.add(new CouchDbAuditActionContext(audit));
    }

    @Override
    public Set<? extends AuditActionContext> getAuditRecords(final Map<WhereClauseFields, Object> whereClause) {
        return CollectionUtils.wrapHashSet(couchDb.findAuditRecords(whereClause));
    }
}
