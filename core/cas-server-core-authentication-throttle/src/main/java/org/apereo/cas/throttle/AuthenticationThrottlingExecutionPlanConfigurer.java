package org.apereo.cas.throttle;

import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link AuthenticationThrottlingExecutionPlanConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@FunctionalInterface
public interface AuthenticationThrottlingExecutionPlanConfigurer {
    /**
     * Configure authentication throttling execution plan.
     *
     * @param plan the plan
     */
    void configureAuthenticationThrottlingExecutionPlan(AuthenticationThrottlingExecutionPlan plan);

    /**
     * Gets name.
     *
     * @return the name
     */
    default String getName() {
        return StringUtils.defaultIfBlank(this.getClass().getSimpleName(), "Default");
    }
}
