package org.apereo.cas.authentication;

import org.apereo.cas.services.ServiceContext;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * Produces authentication policies that passively satisfy any given {@link Authentication}.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@RefreshScope
@Component("acceptAnyAuthenticationPolicyFactory")
public class AcceptAnyAuthenticationPolicyFactory implements ContextualAuthenticationPolicyFactory<ServiceContext> {

    @Override
    public ContextualAuthenticationPolicy<ServiceContext> createPolicy(final ServiceContext context) {
        return new ContextualAuthenticationPolicy<ServiceContext>() {

            @Override
            public ServiceContext getContext() {
                return context;
            }

            @Override
            public boolean isSatisfiedBy(final Authentication authentication) {
                return true;
            }
        };
    }
}
