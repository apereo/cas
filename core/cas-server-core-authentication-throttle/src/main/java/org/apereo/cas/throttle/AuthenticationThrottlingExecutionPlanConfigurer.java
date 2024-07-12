package org.apereo.cas.throttle;

import org.apereo.cas.util.NamedObject;

/**
 * This is {@link AuthenticationThrottlingExecutionPlanConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@FunctionalInterface
public interface AuthenticationThrottlingExecutionPlanConfigurer extends NamedObject {
    /**
     * Configure authentication throttling execution plan.
     *
     * @param plan the plan
     */
    void configureAuthenticationThrottlingExecutionPlan(AuthenticationThrottlingExecutionPlan plan);
    
}
