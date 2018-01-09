package org.apereo.cas.audit;

import org.apereo.cas.util.DateTimeUtils;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDate;
import java.util.Date;
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
public class MongoDbAuditTrailManager implements AuditTrailManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDbAuditTrailManager.class);

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private boolean asynchronous = true;

    private final String collectionName;
    private final MongoTemplate mongoTemplate;

    public MongoDbAuditTrailManager(final MongoTemplate mongoTemplate, final String collectionName) {
        this.mongoTemplate = mongoTemplate;
        this.collectionName = collectionName;
    }

    public void setAsynchronous(final boolean asynchronous) {
        this.asynchronous = asynchronous;
    }

    @Override
    public void record(final AuditActionContext audit) {
        if (this.asynchronous) {
            this.executorService.execute(() -> {
                this.mongoTemplate.save(audit, this.collectionName);
            });
        } else {
            this.mongoTemplate.save(audit, this.collectionName);
        }
    }

    @Override
    public Set<AuditActionContext> getAuditRecordsSince(final LocalDate localDate) {
        final Date dt = DateTimeUtils.dateOf(localDate);
        final Query query = new Query().addCriteria(Criteria.where("whenActionWasPerformed").lte(dt));
        return new LinkedHashSet<>(this.mongoTemplate.find(query, AuditActionContext.class, this.collectionName));
    }
}
