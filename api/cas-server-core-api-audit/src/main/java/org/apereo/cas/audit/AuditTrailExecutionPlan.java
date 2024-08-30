package org.apereo.cas.audit;

import org.apereo.inspektr.audit.AuditActionContext;
import org.apereo.inspektr.audit.AuditTrailManager;

import java.util.List;
import java.util.Map;

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
}
