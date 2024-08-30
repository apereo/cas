package org.apereo.cas.audit.spi;

import lombok.Getter;
import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
        val localDate = (LocalDate) whereClause.get(WhereClauseFields.DATE);
        return auditRecords
            .stream()
            .filter(audit -> audit.getWhenActionWasPerformed().isAfter(localDate.atStartOfDay()))
            .collect(Collectors.toList());
    }

    @Override
    public void removeAll() {
        auditRecords.clear();
    }
}
