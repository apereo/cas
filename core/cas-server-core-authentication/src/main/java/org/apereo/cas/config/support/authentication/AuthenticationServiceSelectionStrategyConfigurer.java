package org.apereo.cas.config.support.authentication;

import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;

/**
 * This is {@link AuthenticationServiceSelectionStrategyConfigurer}
 * that is used to extract and translate a given service request/URL.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public interface AuthenticationServiceSelectionStrategyConfigurer {
    /**
     * Configure authentication service selection strategy.
     *
     * @param plan the plan
     */
    void configureAuthenticationServiceSelectionStrategy(AuthenticationServiceSelectionPlan plan);
}
