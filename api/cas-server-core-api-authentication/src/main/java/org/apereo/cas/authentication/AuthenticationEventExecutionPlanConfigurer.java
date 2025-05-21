package org.apereo.cas.authentication;

import org.apereo.cas.util.NamedObject;
import org.springframework.core.Ordered;

/**
 * This is {@link AuthenticationEventExecutionPlanConfigurer}.
 * Passes on an authentication execution plan to implementers
 * to register authentication handlers, etc.
 * <p>
 * Since this interface conforms to a functional interface requirement, typical implementers
 * are {@code @Conditional} beans expressed as compact lambda expressions inside of various CAS modules that
 * contribute to the overall CAS authentication subsystem.
 * <p>
 * Note: Existing configuration classes that are injected authentication-related functionality
 * such as the transaction manager or the authentication support components need to be refactored
 * to isolate those changes into the configurer. Otherwise, circular dependency issues may appear.
 *
 * @author Misagh Moayyed
 * @author Dmitriy Kopylenko
 * @since 5.1.0
 */
@FunctionalInterface
public interface AuthenticationEventExecutionPlanConfigurer extends Ordered, NamedObject {

    /**
     * configure the plan.
     *
     * @param plan the plan
     */
    void configureAuthenticationExecutionPlan(AuthenticationEventExecutionPlan plan) throws Exception;


    @Override
    default int getOrder() {
        return 0;
    }
}
