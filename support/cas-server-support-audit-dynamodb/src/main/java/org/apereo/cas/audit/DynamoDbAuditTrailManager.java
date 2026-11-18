package org.apereo.cas.audit;

import module java.base;
import org.apereo.cas.audit.spi.AbstractAuditTrailManager;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditActionCount;
import org.apereo.inspektr.audit.AuditSummaryQuery;

/**
 * This is {@link DynamoDbAuditTrailManager}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Setter
@RequiredArgsConstructor
public class DynamoDbAuditTrailManager extends AbstractAuditTrailManager {
    private final DynamoDbAuditTrailManagerFacilitator dynamoDbFacilitator;

    public DynamoDbAuditTrailManager(final DynamoDbAuditTrailManagerFacilitator facilitator,
                                     final boolean asynchronous) {
        super(asynchronous);
        this.dynamoDbFacilitator = facilitator;
    }

    @Override
    protected void saveAuditRecord(final AuditActionContext audit) {
        this.dynamoDbFacilitator.save(audit);
        this.dynamoDbFacilitator.count(audit);
    }

    @Override
    public Optional<List<AuditActionCount>> summarize(final AuditSummaryQuery query) {
        return Optional.of(dynamoDbFacilitator.summarize(query));
    }

    @Override
    public List<? extends AuditActionContext> getAuditRecords(final Map<WhereClauseFields, Object> whereClause) {
        return dynamoDbFacilitator.getAuditRecords(whereClause);
    }

    @Override
    public void removeAll() {
        this.dynamoDbFacilitator.removeAll();
    }
}
