package org.apereo.cas.authentication.principal;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;

/**
 * This is {@link PrincipalResolutionExecutionPlanConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@FunctionalInterface
public interface PrincipalResolutionExecutionPlanConfigurer extends Ordered {

    /**
     * configure the plan.
     *
     * @param plan the plan
     */
    void configurePrincipalResolutionExecutionPlan(PrincipalResolutionExecutionPlan plan);

    /**
     * Gets name.
     *
     * @return the name
     */
    default String getName() {
        return StringUtils.defaultIfBlank(this.getClass().getSimpleName(), "Default");
    }

    @Override
    default int getOrder() {
        return 0;
    }
}
