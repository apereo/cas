package org.apereo.cas.authentication;

import org.apereo.cas.util.NamedObject;

/**
 * This is {@link AuthenticationServiceSelectionStrategyConfigurer}
 * that is used to extract and translate a given service request/URL.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@FunctionalInterface
public interface AuthenticationServiceSelectionStrategyConfigurer extends NamedObject {
    /**
     * Configure authentication service selection strategy.
     *
     * @param plan the plan
     */
    void configureAuthenticationServiceSelectionStrategy(AuthenticationServiceSelectionPlan plan);
}
