package org.apereo.cas.authentication.policy;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.ContextualAuthenticationPolicy;
import org.apereo.cas.authentication.ContextualAuthenticationPolicyFactory;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAuthenticationPolicy;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/**
 * Produces {@link ContextualAuthenticationPolicy} instances that are satisfied iff the given {@link Authentication}
 * was created by authenticating credentials by all handlers named in
 * {@link RegisteredServiceAuthenticationPolicy#getRequiredAuthenticationHandlers()}.
 *
 * @author Marvin S. Addison
 * @since 4.0.0
 */
@Slf4j
public class RequiredHandlerAuthenticationPolicyFactory implements ContextualAuthenticationPolicyFactory<RegisteredService> {

    @Override
    public ContextualAuthenticationPolicy<RegisteredService> createPolicy(final RegisteredService registeredService) {
        return new ContextualAuthenticationPolicy<>() {

            @Override
            public RegisteredService getContext() {
                return registeredService;
            }

            @Override
            public boolean isSatisfiedBy(final Authentication authentication) {
                val requiredHandlers = registeredService.getAuthenticationPolicy().getRequiredAuthenticationHandlers();
                LOGGER.debug("Required authentication handlers for this service [{}] are [{}]",
                    registeredService.getName(), requiredHandlers);
                return requiredHandlers.stream().allMatch(required -> authentication.getSuccesses().containsKey(required));
            }
        };
    }
}
