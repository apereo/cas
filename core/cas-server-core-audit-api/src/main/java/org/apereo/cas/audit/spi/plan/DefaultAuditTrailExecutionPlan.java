package org.apereo.cas.audit.spi.plan;

import org.apereo.cas.audit.AuditTrailExecutionPlan;
import org.apereo.cas.util.spring.beans.BeanSupplier;

import lombok.Getter;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link DefaultAuditTrailExecutionPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Getter
public class DefaultAuditTrailExecutionPlan implements AuditTrailExecutionPlan {
    private final List<AuditTrailManager> auditTrailManagers = new ArrayList<>(0);

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
    public Set<AuditActionContext> getAuditRecords(final Map<AuditTrailManager.WhereClauseFields, Object> criteria) {
        return auditTrailManagers
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .map(manager -> manager.getAuditRecords(criteria))
            .flatMap(Set::stream)
            .collect(Collectors.toSet());
    }
}
