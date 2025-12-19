package org.apereo.cas.audit;

import module java.base;
import org.apereo.cas.audit.spi.AbstractAuditTrailManager;
import org.apereo.cas.util.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

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

    private final MongoOperations mongoTemplate;
    private final String collectionName;

    public MongoDbAuditTrailManager(final MongoOperations mongoTemplate, final String collectionName, final boolean asynchronous) {
        super(asynchronous);
        this.mongoTemplate = mongoTemplate;
        this.collectionName = collectionName;
    }

    @Override
    protected void saveAuditRecord(final AuditActionContext audit) {
        this.mongoTemplate.save(audit, this.collectionName);
    }

    @Override
    public List<? extends AuditActionContext> getAuditRecords(final Map<WhereClauseFields, Object> whereClause) {
        val localDate = (LocalDateTime) whereClause.get(WhereClauseFields.DATE);
        val dt = DateTimeUtils.dateOf(localDate);
        LOGGER.debug("Retrieving audit records since [{}] from [{}]", dt, this.collectionName);
        val query = new Query().addCriteria(Criteria.where("whenActionWasPerformed").gte(dt));
        if (whereClause.containsKey(WhereClauseFields.PRINCIPAL)) {
            query.addCriteria(Criteria.where("principal").is(whereClause.get(WhereClauseFields.PRINCIPAL).toString()));
        }
        if (whereClause.containsKey(WhereClauseFields.COUNT)) {
            query.limit(Long.valueOf(whereClause.get(WhereClauseFields.COUNT).toString()).intValue());
        }
        return new ArrayList<>(this.mongoTemplate.find(query, AuditActionContext.class, this.collectionName));
    }

    @Override
    public void removeAll() {
        this.mongoTemplate.remove(new Query(), AuditActionContext.class, this.collectionName);
    }
}
