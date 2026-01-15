package org.apereo.cas.audit.spi;

import module java.base;
import lombok.Getter;
import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;

/**
 * This is {@link MockAuditTrailManager}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Getter
public class MockAuditTrailManager implements AuditTrailManager {
    private final Set<AuditActionContext> auditRecords = new LinkedHashSet<>();

    @Override
    public void record(final AuditActionContext auditActionContext) {
        auditRecords.add(auditActionContext);
    }

    @Override
    public List<? extends AuditActionContext> getAuditRecords(final Map<WhereClauseFields, Object> whereClause) {
        val localDate = (LocalDateTime) whereClause.get(WhereClauseFields.DATE);
        return auditRecords
            .stream()
            .filter(audit -> audit.getWhenActionWasPerformed().isAfter(localDate))
            .collect(Collectors.toList());
    }

    @Override
    public void removeAll() {
        auditRecords.clear();
    }
}
