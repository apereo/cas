package org.apereo.cas.audit;

import org.apereo.cas.audit.spi.AbstractAuditTrailManager;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apereo.inspektr.audit.AuditActionContext;

import java.util.List;
import java.util.Map;

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
