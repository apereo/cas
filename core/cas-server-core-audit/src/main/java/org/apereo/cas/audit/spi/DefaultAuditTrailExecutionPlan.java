package org.apereo.cas.audit.spi;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultAuditTrailExecutionPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
@Getter
public class DefaultAuditTrailExecutionPlan implements AuditTrailExecutionPlan {
    private List<AuditTrailManager> auditTrailManagers = new ArrayList<>();

    @Override
    public void registerAuditTrailManager(final AuditTrailManager manager) {
        this.auditTrailManagers.add(manager);
    }

    @Override
    public void record(final AuditActionContext audit) {
        this.auditTrailManagers.forEach(manager -> manager.record(audit));
    }

    @Override
    public Set<AuditActionContext> getAuditRecordsSince(final LocalDate sinceDate) {
        return this.auditTrailManagers
            .stream()
            .map(manager -> manager.getAuditRecordsSince(sinceDate))
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }
}
