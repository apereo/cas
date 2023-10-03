package org.apereo.cas.authentication.policy;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.ContextualAuthenticationPolicy;
import org.apereo.cas.authentication.ContextualAuthenticationPolicyFactory;
import org.apereo.cas.services.RegisteredService;


/**
 * Produces authentication policies that passively satisfy any given {@link Authentication}.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class AcceptAnyAuthenticationPolicyFactory implements ContextualAuthenticationPolicyFactory<RegisteredService> {
    @Override
    public ContextualAuthenticationPolicy<RegisteredService> createPolicy(final RegisteredService registeredService) {
        return new ContextualAuthenticationPolicy<>() {
            @Override
            public RegisteredService getContext() {
                return registeredService;
            }

            @Override
            public boolean isSatisfiedBy(final Authentication authentication) {
                return true;
            }
        };
    }
}
