package org.apereo.cas.audit;

import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * This is {@link AuditTrailExecutionPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public interface AuditTrailExecutionPlan {

    /**
     * Register audit trail manager.
     *
     * @param manager the manager
     */
    void registerAuditTrailManager(AuditTrailManager manager);

    /**
     * Gets audit trail managers.
     *
     * @return the audit trail managers
     */
    List<AuditTrailManager> getAuditTrailManagers();

    /**
     * Record.
     *
     * @param audit the audit
     */
    void record(AuditActionContext audit);

    /**
     * Gets audit records since the specified date.
     *
     * @param sinceDate the since date
     * @return the audit records since
     */
    Set<AuditActionContext> getAuditRecordsSince(LocalDate sinceDate);
}
