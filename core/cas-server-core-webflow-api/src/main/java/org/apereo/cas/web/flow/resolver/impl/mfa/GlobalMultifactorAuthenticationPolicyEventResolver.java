package org.apereo.cas.web.flow.resolver.impl.mfa;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.authentication.BaseMultifactorAuthenticationProviderEventResolver;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link GlobalMultifactorAuthenticationPolicyEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class GlobalMultifactorAuthenticationPolicyEventResolver extends BaseMultifactorAuthenticationProviderEventResolver {

    private final String globalProviderId;

    public GlobalMultifactorAuthenticationPolicyEventResolver(final AuthenticationSystemSupport authenticationSystemSupport,
                                                              final CentralAuthenticationService centralAuthenticationService,
                                                              final ServicesManager servicesManager,
                                                              final TicketRegistrySupport ticketRegistrySupport,
                                                              final CookieGenerator warnCookieGenerator,
                                                              final AuthenticationServiceSelectionPlan authenticationSelectionStrategies,
                                                              final MultifactorAuthenticationProviderSelector selector,
                                                              final CasConfigurationProperties casProperties) {
        super(authenticationSystemSupport, centralAuthenticationService, servicesManager,
                ticketRegistrySupport, warnCookieGenerator,
                authenticationSelectionStrategies, selector);
        globalProviderId = casProperties.getAuthn().getMfa().getGlobalProviderId();
    }

    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        final RegisteredService service = resolveRegisteredServiceInRequestContext(context);
        final Authentication authentication = WebUtils.getAuthentication(context);

        if (authentication == null) {
            LOGGER.debug("No authentication is available to determine event for principal");
            return null;
        }
        if (StringUtils.isBlank(globalProviderId)) {
            LOGGER.debug("No value could be found for request parameter [{}]", globalProviderId);
            return null;
        }
        LOGGER.debug("Attempting to globally activate [{}]", globalProviderId);

        final Map<String, MultifactorAuthenticationProvider> providerMap =
                MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(this.applicationContext);
        if (providerMap == null || providerMap.isEmpty()) {
            LOGGER.error("No multifactor authentication providers are available in the application context to handle [{}]", globalProviderId);
            throw new AuthenticationException();
        }

        final Optional<MultifactorAuthenticationProvider> providerFound = resolveProvider(providerMap, globalProviderId);
        if (providerFound.isPresent()) {
            final MultifactorAuthenticationProvider provider = providerFound.get();
            LOGGER.debug("Attempting to build an event based on the authentication provider [{}] and service [{}]", provider, service);
            final Map<String, Object> attributes = buildEventAttributeMap(authentication.getPrincipal(), service, provider);
            final Event event = validateEventIdForMatchingTransitionInContext(provider.getId(), context, attributes);
            return CollectionUtils.wrapSet(event);
        }
        LOGGER.warn("No multifactor provider could be found for [{}]", globalProviderId);
        throw new AuthenticationException();
    }


    @Audit(action = "AUTHENTICATION_EVENT", actionResolverName = "AUTHENTICATION_EVENT_ACTION_RESOLVER",
            resourceResolverName = "AUTHENTICATION_EVENT_RESOURCE_RESOLVER")
    @Override
    public Event resolveSingle(final RequestContext context) {
        return super.resolveSingle(context);
    }
}
