package org.jasig.cas.web.flow.authentication;

import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.authentication.AuthenticationException;
import org.jasig.cas.services.RegisteredServiceMultifactorAuthenticationProvider;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.RegisteredServiceAuthenticationPolicy;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.stereotype.Component;
import org.springframework.webflow.definition.TransitionDefinition;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Set;

/**
 * This is {@link RegisteredServiceAuthenticationPolicyWebflowEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Component("registeredServiceAuthenticationPolicyWebflowEventResolver")
public class RegisteredServiceAuthenticationPolicyWebflowEventResolver extends AbstractCasWebflowEventResolver {

    @Override
    protected Event resolveInternal(final RequestContext context) {
        final RegisteredService service = WebUtils.getRegisteredService(context);

        final RegisteredServiceAuthenticationPolicy policy = service.getAuthenticationPolicy();
        if (service.getAuthenticationPolicy().getMultifactorAuthenticationProviders().isEmpty()) {
            return null;
        }
        final Set<String> providers = policy.getMultifactorAuthenticationProviders();
        for (final String provider : providers) {
            final RegisteredServiceMultifactorAuthenticationProvider providerClass = loadMultifactorAuthenticationProvider(provider);
            if (providerClass == null) {
                logger.warn("Could not locate [{}] bean id in the application context as an authentication provider. "
                        + "Are you missing a dependency in your configuration?", provider);
                throw new IllegalArgumentException("Could not locate " + provider + " in the application configuration");
            }
            final Event event = resolveEventPerAuthenticationProvider(context, service, providerClass);
            if (event != null) {
                logger.debug("Resolved authentication event {} based on authentication provider {}", event, providerClass);
                return event;
            }
        }

        logger.warn("No authentication provider could be resoled for this registered service");
        return null;
    }

    private Event resolveEventPerAuthenticationProvider(final RequestContext context, final RegisteredService service,
                                                        final RegisteredServiceMultifactorAuthenticationProvider provider) {

        try {
            final String identifier = provider.provide(service);
            if (StringUtils.isBlank(identifier)) {
                logger.warn("Multifactor authentication provider {} could not provide CAS with its identifier.", provider);
                return null;
            }
            logger.debug("Attempting to build an event based on the authentication provider [{}] and service [{}]",
                    provider, service.getName());

            final Event event = new Event(this, identifier);
            logger.debug("Resulting event id is [{}]. Locating transitions in the context for that event id...",
                    event.getId());

            final TransitionDefinition def = context.getMatchingTransition(event.getId());
            if (def == null) {
                logger.warn("Transition definition cannot be found for event [{}]", event.getId());
                throw new AuthenticationException();
            }
            logger.debug("Found matching transition [{}] with target [{}] for event [{}].",
                    def.getId(), def.getTargetStateId(), event.getId());


            return event;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private RegisteredServiceMultifactorAuthenticationProvider loadMultifactorAuthenticationProvider(final String provider) {
        try {
            return this.applicationContext.getBean(provider, RegisteredServiceMultifactorAuthenticationProvider.class);
        } catch (final Exception e) {
            logger.warn(e.getMessage(), e);
        }
        return null;
    }
}
