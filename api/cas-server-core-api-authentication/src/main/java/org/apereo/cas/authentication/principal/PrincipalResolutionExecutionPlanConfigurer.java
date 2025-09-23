package org.apereo.cas.authentication.principal;

import org.apereo.cas.util.NamedObject;
import org.springframework.core.Ordered;

/**
 * This is {@link PrincipalResolutionExecutionPlanConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@FunctionalInterface
public interface PrincipalResolutionExecutionPlanConfigurer extends Ordered, NamedObject {

    /**
     * configure the plan.
     *
     * @param plan the plan
     */
    void configurePrincipalResolutionExecutionPlan(PrincipalResolutionExecutionPlan plan);

    @Override
    default int getOrder() {
        return 0;
    }
}
