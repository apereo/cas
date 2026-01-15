package org.apereo.cas.web.flow;
import module java.base;

/**
 * This is {@link CasWebflowExecutionPlanConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@FunctionalInterface
public interface CasWebflowExecutionPlanConfigurer {
    /**
     * Configure webflow execution plan.
     *
     * @param plan the plan
     */
    void configureWebflowExecutionPlan(CasWebflowExecutionPlan plan);
}
