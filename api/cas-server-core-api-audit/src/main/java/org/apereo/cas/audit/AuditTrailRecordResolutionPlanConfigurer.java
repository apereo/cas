package org.apereo.cas.audit;

import module java.base;
import org.apereo.cas.util.NamedObject;
import org.springframework.core.Ordered;

/**
 * This is {@link AuditTrailRecordResolutionPlanConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@FunctionalInterface
public interface AuditTrailRecordResolutionPlanConfigurer extends Ordered, NamedObject {

    /**
     * Configure audit trail resolution plan.
     *
     * @param plan the plan
     */
    void configureAuditTrailRecordResolutionPlan(AuditTrailRecordResolutionPlan plan);

    @Override
    default int getOrder() {
        return 0;
    }
}
