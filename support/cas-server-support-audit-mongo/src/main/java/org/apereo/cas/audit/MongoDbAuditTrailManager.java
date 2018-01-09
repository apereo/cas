package org.apereo.cas.audit;

import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * This is {@link MongoDbAuditTrailManager}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class MongoDbAuditTrailManager implements AuditTrailManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(MongoDbAuditTrailManager.class);

    private final String collectionName;
    private final MongoTemplate mongoTemplate;

    public MongoDbAuditTrailManager(final MongoTemplate mongoTemplate, final String collectionName) {
        this.mongoTemplate = mongoTemplate;
        this.collectionName = collectionName;
    }

    @Override
    public Set<AuditActionContext> getAuditRecordsSince(final LocalDate localDate) {
        return new HashSet<>(0);
    }

    @Override
    public void record(final AuditActionContext audit) {
        this.mongoTemplate.save(audit, this.collectionName);
    }
}
