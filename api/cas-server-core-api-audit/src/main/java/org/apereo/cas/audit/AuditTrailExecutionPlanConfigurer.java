package org.apereo.cas.audit;

import org.apereo.cas.util.NamedObject;

/**
 * This is {@link AuditTrailExecutionPlanConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@FunctionalInterface
public interface AuditTrailExecutionPlanConfigurer extends NamedObject {

    /**
     * Configure audit trail execution plan.
     *
     * @param plan the plan
     */
    void configureAuditTrailExecutionPlan(AuditTrailExecutionPlan plan);
}
