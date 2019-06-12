package org.apereo.cas.audit;

import org.apereo.cas.audit.spi.AbstractAuditTrailManager;
import org.apereo.cas.util.DateTimeUtils;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This is {@link MongoDbAuditTrailManager}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@Setter
@RequiredArgsConstructor
public class MongoDbAuditTrailManager extends AbstractAuditTrailManager {

    private final transient MongoTemplate mongoTemplate;
    private final String collectionName;

    public MongoDbAuditTrailManager(final MongoTemplate mongoTemplate, final String collectionName, final boolean asynchronous) {
        super(asynchronous);
        this.mongoTemplate = mongoTemplate;
        this.collectionName = collectionName;
    }

    @Override
    protected void saveAuditRecord(final AuditActionContext audit) {
        this.mongoTemplate.save(audit, this.collectionName);
    }

    @Override
    public Set<? extends AuditActionContext> getAuditRecordsSince(final LocalDate localDate) {
        val dt = DateTimeUtils.dateOf(localDate);
        LOGGER.debug("Retrieving audit records since [{}] from [{}]", dt, this.collectionName);
        val query = new Query().addCriteria(Criteria.where("whenActionWasPerformed").gte(dt));
        return new LinkedHashSet<>(this.mongoTemplate.find(query, AuditActionContext.class, this.collectionName));
    }

    @Override
    public void removeAll() {
        this.mongoTemplate.remove(new Query(), AuditActionContext.class, this.collectionName);
    }
}
