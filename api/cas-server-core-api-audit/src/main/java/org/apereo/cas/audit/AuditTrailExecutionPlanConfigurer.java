package org.apereo.cas.audit;

/**
 * This is {@link AuditTrailExecutionPlanConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@FunctionalInterface
public interface AuditTrailExecutionPlanConfigurer {

    /**
     * Configure audit trail execution plan.
     *
     * @param plan the plan
     */
    void configureAuditTrailExecutionPlan(AuditTrailExecutionPlan plan);
    /**
     * Gets name.
     *
     * @return the name
     */
    default String getName() {
        return getClass().getSimpleName();
    }
}
