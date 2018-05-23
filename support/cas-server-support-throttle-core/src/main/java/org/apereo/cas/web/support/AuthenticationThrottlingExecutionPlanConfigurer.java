package org.apereo.cas.web.support;

/**
 * This is {@link AuthenticationThrottlingExecutionPlanConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public interface AuthenticationThrottlingExecutionPlanConfigurer {
    /**
     * Configure authentication throttling execution plan.
     *
     * @param plan the plan
     */
    default void configureAuthenticationThrottlingExecutionPlan(AuthenticationThrottlingExecutionPlan plan) {
    }
}
