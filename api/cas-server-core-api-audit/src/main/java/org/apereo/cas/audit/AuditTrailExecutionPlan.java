package org.apereo.cas.audit;

import module java.base;
import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditActionCount;
import org.apereo.inspektr.audit.AuditSummaryQuery;
import org.apereo.inspektr.audit.AuditTrailManager;

/**
 * This is {@link AuditTrailExecutionPlan}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public interface AuditTrailExecutionPlan {
    /**
     * Bean name.
     */
    String BEAN_NAME = "auditTrailExecutionPlan";

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
     * Gets audit records for the specified query.
     *
     * @param criteria the criteria
     * @return the audit records since
     */
    List<AuditActionContext> getAuditRecords(Map<AuditTrailManager.WhereClauseFields, Object> criteria);

    /**
     * Count records per action and per hour with the first audit storage that can, so storages that keep the same
     * records are never added together.
     *
     * @param query the period and actions to count
     * @return the counts; empty when no audit storage can summarize
     */
    default Optional<List<AuditActionCount>> summarize(final AuditSummaryQuery query) {
        return getAuditTrailManagers()
            .stream()
            .map(manager -> manager.summarize(query))
            .flatMap(Optional::stream)
            .findFirst();
    }
}
