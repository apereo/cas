package org.apereo.cas.audit;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This is {@link MongoDbAuditTrailManager}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@Setter
@RequiredArgsConstructor
public class MongoDbAuditTrailManager implements AuditTrailManager {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Setter
    private boolean asynchronous = true;

    private final transient MongoTemplate mongoTemplate;
    private final String collectionName;


    @Override
    public void record(final AuditActionContext audit) {
        if (this.asynchronous) {
            this.executorService.execute(() -> saveAuditRecord(audit));
        } else {
            saveAuditRecord(audit);
        }
    }

    private void saveAuditRecord(final AuditActionContext audit) {
        this.mongoTemplate.save(audit, this.collectionName);
    }

    @Override
    public Set<AuditActionContext> getAuditRecordsSince(final LocalDate localDate) {
        final var dt = DateTimeUtils.dateOf(localDate);
        LOGGER.debug("Retrieving audit records since [{}] from [{}]", dt, this.collectionName);
        final var query = new Query().addCriteria(Criteria.where("whenActionWasPerformed").gte(dt));
        return new LinkedHashSet<>(this.mongoTemplate.find(query, AuditActionContext.class, this.collectionName));
    }
}
