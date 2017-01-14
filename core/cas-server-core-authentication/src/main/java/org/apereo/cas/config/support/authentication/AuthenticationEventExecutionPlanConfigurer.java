package org.apereo.cas.config.support.authentication;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;

/**
 * This is {@link AuthenticationEventExecutionPlanConfigurer}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface AuthenticationEventExecutionPlanConfigurer {

    /**
     * Register authentication handler.
     *
     * @param plan the plan
     */
    default void configureAuthenticationExecutionPlan(final AuthenticationEventExecutionPlan plan) {}
}
