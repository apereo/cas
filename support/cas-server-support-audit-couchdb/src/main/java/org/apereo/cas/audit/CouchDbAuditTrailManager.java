package org.apereo.cas.audit;

import org.apereo.cas.couchdb.AuditActionContextCouchDbRepository;
import org.apereo.cas.couchdb.CouchDbAuditActionContext;

import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;

import java.time.LocalDate;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This is {@link CouchDbAuditTrailManager}.
 *
 * @author Timur Duehr
 * @since 6.0.0
 */
@AllArgsConstructor
@Getter
@Setter
public class CouchDbAuditTrailManager implements AuditTrailManager {
    @NonNull
    private AuditActionContextCouchDbRepository couchDb;

    private boolean asyncronous;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public void record(final AuditActionContext auditActionContext) {
        if (asyncronous) {
            this.executorService.execute(() -> couchDb.add(new CouchDbAuditActionContext(auditActionContext)));
        } else {
            couchDb.add(new CouchDbAuditActionContext(auditActionContext));
        }
    }

    @Override
    public Set<AuditActionContext> getAuditRecordsSince(final LocalDate localDate) {
        return Sets.newHashSet(couchDb.findAuditRecordsSince(localDate));
    }
}
