package org.jasig.cas.authentication;

import org.jasig.cas.services.ServiceContext;
import org.springframework.stereotype.Component;

/**
 * Produces {@link ContextualAuthenticationPolicy} instances that are satisfied iff the given {@link Authentication}
 * was created by authenticating credentials by all handlers named in
 * {@link org.jasig.cas.services.RegisteredService#getRequiredHandlers()}.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Component("requiredHandlerAuthenticationPolicyFactory")
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
                for (final String required : context.getRegisteredService().getRequiredHandlers()) {
                    if (!authentication.getSuccesses().containsKey(required)) {
                        return false;
                    }
                }
                return true;
            }
        };
    }
}
