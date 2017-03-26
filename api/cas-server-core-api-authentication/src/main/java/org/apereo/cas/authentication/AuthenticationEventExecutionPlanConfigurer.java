package org.apereo.cas.authentication;

/**
 * This is {@link AuthenticationEventExecutionPlanConfigurer}.
 * Passes on an authentication execution plan to implementors
 * to register authentication handlers, etc. This class is typically
 * implemented by a configuration class inside a CAS module.
 * <p>
 * Note: Existing configuration classes that are injected authentication-related functionality
 * such as the transaction manager or the authentication support components need to be refactored
 * to isolate those changes into the configurer. Otherwise, circular dependency issues may appear.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface AuthenticationEventExecutionPlanConfigurer {

    /**
     * configure the plan.
     *
     * @param plan the plan
     */
    default void configureAuthenticationExecutionPlan(final AuthenticationEventExecutionPlan plan) {
    }
}
