package org.apereo.cas.audit.spi;

import module java.base;
import lombok.Getter;
import lombok.val;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditActionCount;
import org.apereo.inspektr.audit.AuditSummaryQuery;
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
    public Optional<List<AuditActionCount>> summarize(final AuditSummaryQuery query) {
        val counts = auditRecords
            .stream()
            .filter(audit -> query.actions().contains(audit.getActionPerformed()))
            .filter(audit -> !audit.getWhenActionWasPerformed().isBefore(query.from()) && audit.getWhenActionWasPerformed().isBefore(query.to()))
            .collect(Collectors.groupingBy(audit -> List.of(AuditSummaryQuery.hourOf(audit.getWhenActionWasPerformed()), audit.getActionPerformed()),
                Collectors.counting()));
        return Optional.of(counts.entrySet()
            .stream()
            .map(entry -> new AuditActionCount((LocalDateTime) entry.getKey().getFirst(), (String) entry.getKey().getLast(), entry.getValue()))
            .toList());
    }

    @Override
    public void removeAll() {
        auditRecords.clear();
    }
}
