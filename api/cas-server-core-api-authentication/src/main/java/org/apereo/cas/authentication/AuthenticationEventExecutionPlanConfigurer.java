package org.apereo.cas.authentication;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;

/**
 * This is {@link AuthenticationEventExecutionPlanConfigurer}.
 * Passes on an authentication execution plan to implementors
 * to register authentication handlers, etc.
 * <p>
 * Since this interface conforms to a functional interface requirement, typical implementors
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
public interface AuthenticationEventExecutionPlanConfigurer extends Ordered {

    /**
     * configure the plan.
     *
     * @param plan the plan
     */
    void configureAuthenticationExecutionPlan(AuthenticationEventExecutionPlan plan);

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
