package org.apereo.cas.audit;

import module java.base;
import org.apereo.cas.audit.spi.AbstractAuditTrailManager;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditActionCount;
import org.apereo.inspektr.audit.AuditSummaryQuery;
import org.bson.Document;
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
        val dt = toStoredDate(Objects.requireNonNull(localDate));
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

    /**
     * Counts records with one aggregation: the records of the period and actions are grouped by action and by the
     * number of hours between the start of the period and the record. The period is converted to dates the way records
     * are written, so it lines up with them whatever the server's time zone.
     *
     * @param query the period and actions to count
     * @return the counts
     */
    @Override
    public Optional<List<AuditActionCount>> summarize(final AuditSummaryQuery query) {
        val hours = query.hours();
        if (hours.isEmpty() || query.actions().isEmpty()) {
            return Optional.of(List.of());
        }
        val from = toStoredDate(query.from());
        val match = new Document("$match", new Document("whenActionWasPerformed",
            new Document("$gte", from).append("$lt", toStoredDate(query.to())))
            .append("actionPerformed", new Document("$in", List.copyOf(query.actions()))));
        val bucket = new Document("$floor", new Document("$divide",
            List.of(new Document("$subtract", List.of("$whenActionWasPerformed", from)), AuditSummaryQuery.BUCKET.toMillis())));
        val group = new Document("$group", new Document("_id",
            new Document("action", "$actionPerformed").append("bucket", bucket))
            .append("count", new Document("$sum", 1)));
        val counts = new ArrayList<AuditActionCount>();
        mongoTemplate.getCollection(collectionName).aggregate(List.of(match, group)).forEach(result -> {
            val id = Objects.requireNonNull(result.get("_id", Document.class));
            val index = ((Number) Objects.requireNonNull(id.get("bucket"))).intValue();
            if (index >= 0 && index < hours.size()) {
                counts.add(new AuditActionCount(hours.get(index), Objects.requireNonNull(id.getString("action")),
                    ((Number) Objects.requireNonNull(result.get("count"))).longValue()));
            }
        });
        return Optional.of(counts);
    }

    private Object toStoredDate(final LocalDateTime time) {
        return Objects.requireNonNull(mongoTemplate.getConverter().convertToMongoType(time));
    }

    @Override
    public void removeAll() {
        this.mongoTemplate.remove(new Query(), AuditActionContext.class, this.collectionName);
    }
}
