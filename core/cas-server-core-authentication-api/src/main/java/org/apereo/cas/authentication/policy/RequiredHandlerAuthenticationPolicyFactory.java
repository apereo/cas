package org.apereo.cas.authentication.policy;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.ContextualAuthenticationPolicy;
import org.apereo.cas.authentication.ContextualAuthenticationPolicyFactory;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServiceContext;

/**
 * Produces {@link ContextualAuthenticationPolicy} instances that are satisfied iff the given {@link Authentication}
 * was created by authenticating credentials by all handlers named in
 * {@link RegisteredService#getRequiredHandlers()}.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
public class RequiredHandlerAuthenticationPolicyFactory implements ContextualAuthenticationPolicyFactory<ServiceContext> {

    @Override
    public ContextualAuthenticationPolicy<ServiceContext> createPolicy(final ServiceContext context) {
        return new ContextualAuthenticationPolicy<ServiceContext>() {

            @Override
            public ServiceContext getContext() {
                return context;
            }

            @Override
            public boolean isSatisfiedBy(final Authentication authentication) {
                return context.getRegisteredService().getRequiredHandlers().stream()
                        .allMatch(required -> authentication.getSuccesses().containsKey(required));
            }
        };
    }
}
