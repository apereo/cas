package org.apereo.cas.audit.spi.plan;

import module java.base;
import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import lombok.Getter;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;

/**
 * This is {@link DefaultAuditTrailExecutionPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
public class DefaultAuditTrailExecutionPlan implements AuditTrailExecutionPlan {
    private final List<AuditTrailManager> auditTrailManagers = new ArrayList<>();

    @Override
    public void registerAuditTrailManager(final AuditTrailManager manager) {
        if (BeanSupplier.isNotProxy(manager)) {
            this.auditTrailManagers.add(manager);
        }
    }

    @Override
    public void record(final AuditActionContext audit) {
        this.auditTrailManagers.forEach(manager -> manager.record(audit));
    }

    @Override
    public List<AuditActionContext> getAuditRecords(final Map<AuditTrailManager.WhereClauseFields, Object> criteria) {
        return auditTrailManagers
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .map(manager -> manager.getAuditRecords(criteria))
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }
}
