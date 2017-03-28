package org.apereo.cas.logout;

/**
 * This is {@link LogoutExecutionPlanConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface LogoutExecutionPlanConfigurer {
    /**
     * configure the plan.
     *
     * @param plan the plan
     */
    default void configureLogoutExecutionPlan(final LogoutExecutionPlan plan) {
    }
}
