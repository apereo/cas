package org.apereo.cas.logout;

/**
 * This is {@link LogoutExecutionPlanConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@FunctionalInterface
public interface LogoutExecutionPlanConfigurer {
    /**
     * configure the plan.
     *
     * @param plan the plan
     */
    void configureLogoutExecutionPlan(LogoutExecutionPlan plan);
    /**
     * Gets name.
     *
     * @return the name
     */
    default String getName() {
        return getClass().getSimpleName();
    }
}
