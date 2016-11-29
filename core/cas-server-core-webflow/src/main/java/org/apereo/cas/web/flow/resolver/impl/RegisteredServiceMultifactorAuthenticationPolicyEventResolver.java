package org.apereo.cas.web.flow.resolver.impl;

import com.google.common.base.Throwables;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceMultifactorPolicy;
import org.apereo.cas.web.flow.authentication.BaseMultifactorAuthenticationProviderEventResolver;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This is {@link RegisteredServiceMultifactorAuthenticationPolicyEventResolver}
 * that attempts to resolve the next event based on the authentication providers of this service.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class RegisteredServiceMultifactorAuthenticationPolicyEventResolver extends BaseMultifactorAuthenticationProviderEventResolver {

    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        final RegisteredService service = resolveRegisteredServiceInRequestContext(context);
        final Authentication authentication = WebUtils.getAuthentication(context);

        if (service == null || authentication == null) {
            logger.debug("No service or authentication is available to determine event for principal");
            return null;
        }

        final RegisteredServiceMultifactorPolicy policy = service.getMultifactorPolicy();
        if (policy == null || policy.getMultifactorAuthenticationProviders().isEmpty()) {
            logger.debug("Authentication policy does not contain any multifactor authentication providers");
            return null;
        }

        if (StringUtils.isNotBlank(policy.getPrincipalAttributeNameTrigger())
                || StringUtils.isNotBlank(policy.getPrincipalAttributeValueToMatch())) {
            logger.debug("Authentication policy for {} has defined principal attribute triggers. Skipping...");
            return null;
        }

        return resolveEventPerAuthenticationProvider(authentication.getPrincipal(), context, service);
    }

    /**
     * Resolve event per authentication provider event.
     *
     * @param principal the principal
     * @param context   the context
     * @param service   the service
     * @return the event
     */
    protected Set<Event> resolveEventPerAuthenticationProvider(final Principal principal,
                                                               final RequestContext context,
                                                               final RegisteredService service) {
        try {
            final Collection<MultifactorAuthenticationProvider> providers = flattenProviders(getAuthenticationProviderForService(service));
            if (providers != null && !providers.isEmpty()) {
                final MultifactorAuthenticationProvider provider = this.multifactorAuthenticationProviderSelector.resolve(providers, service, principal);

                logger.debug("Selected multifactor authentication provider for this transaction is {}", provider);

                if (!provider.isAvailable(service)) {
                    logger.warn("Multifactor authentication provider {} could not be verified/reached.", provider);
                    return null;
                }
                final String identifier = provider.getId();
                logger.debug("Attempting to build an event based on the authentication provider [{}] and service [{}]", provider, service.getName());

                final Event event = validateEventIdForMatchingTransitionInContext(identifier, context, buildEventAttributeMap(principal, service, provider));
                return new HashSet<>(Collections.singletonList(event));
            }
            logger.debug("No multifactor authentication providers could be located for {}", service);
            return null;
        } catch (final Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Audit(action = "AUTHENTICATION_EVENT", actionResolverName = "AUTHENTICATION_EVENT_ACTION_RESOLVER",
            resourceResolverName = "AUTHENTICATION_EVENT_RESOURCE_RESOLVER")
    @Override
    public Event resolveSingle(final RequestContext context) {
        return super.resolveSingle(context);
    }
}
