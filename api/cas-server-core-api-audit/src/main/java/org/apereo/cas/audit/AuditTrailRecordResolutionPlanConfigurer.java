package org.apereo.cas.audit;

/**
 * This is {@link AuditTrailRecordResolutionPlanConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@FunctionalInterface
public interface AuditTrailRecordResolutionPlanConfigurer {

    /**
     * Configure audit trail resolution plan.
     *
     * @param plan the plan
     */
    void configureAuditTrailRecordResolutionPlan(AuditTrailRecordResolutionPlan plan);

    /**
     * Gets name.
     *
     * @return the name
     */
    default String getName() {
        return getClass().getSimpleName();
    }
}
