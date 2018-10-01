package org.apereo.cas.web.flow.resolver.impl.mfa.adaptive;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationRequest;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationResponse;
import org.apereo.cas.authentication.adaptive.geo.GeoLocationService;
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
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link AdaptiveMultifactorAuthenticationPolicyEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class AdaptiveMultifactorAuthenticationPolicyEventResolver extends BaseMultifactorAuthenticationProviderEventResolver {
    private final GeoLocationService geoLocationService;
    private final Map<String, String> multifactorMap;

    public AdaptiveMultifactorAuthenticationPolicyEventResolver(final AuthenticationSystemSupport authenticationSystemSupport,
                                                                final CentralAuthenticationService centralAuthenticationService,
                                                                final ServicesManager servicesManager, 
                                                                final TicketRegistrySupport ticketRegistrySupport,
                                                                final CookieGenerator warnCookieGenerator,
                                                                final AuthenticationServiceSelectionPlan authenticationSelectionStrategies,
                                                                final MultifactorAuthenticationProviderSelector selector,
                                                                final CasConfigurationProperties casProperties, 
                                                                final GeoLocationService geoLocationService) {
        super(authenticationSystemSupport, centralAuthenticationService, 
                servicesManager, ticketRegistrySupport, warnCookieGenerator,
                authenticationSelectionStrategies, selector);
        this.multifactorMap = casProperties.getAuthn().getAdaptive().getRequireMultifactor();
        this.geoLocationService = geoLocationService;
    }

    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        final RegisteredService service = resolveRegisteredServiceInRequestContext(context);
        final Authentication authentication = WebUtils.getAuthentication(context);

        if (service == null || authentication == null) {
            LOGGER.debug("No service or authentication is available to determine event for principal");
            return null;
        }
        
        if (multifactorMap == null || multifactorMap.isEmpty()) {
            LOGGER.debug("Adaptive authentication is not configured to require multifactor authentication");
            return null;
        }
        
        final Map<String, MultifactorAuthenticationProvider> providerMap =
                MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(this.applicationContext);
        if (providerMap == null || providerMap.isEmpty()) {
            LOGGER.error("No multifactor authentication providers are available in the application context");
            throw new AuthenticationException();
        }
        
        final Set<Event> providerFound = checkRequireMultifactorProvidersForRequest(context, service, authentication);
        if (providerFound != null && !providerFound.isEmpty()) {
            LOGGER.warn("Found multifactor authentication providers [{}] required for this authentication event", providerFound);
            return providerFound;
        }
        
        return null;
    }

    private Set<Event> checkRequireMultifactorProvidersForRequest(final RequestContext context, final RegisteredService service,
                                                                  final Authentication authentication) {
        final ClientInfo clientInfo = ClientInfoHolder.getClientInfo();
        final String clientIp = clientInfo.getClientIpAddress();
        LOGGER.debug("Located client IP address as [{}]", clientIp);

        final String agent = WebUtils.getHttpServletRequestUserAgentFromRequestContext(context);
        final Map<String, MultifactorAuthenticationProvider> providerMap =
                MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(this.applicationContext);
        final Set<Map.Entry<String, String>> entries = multifactorMap.entrySet();
        for (final Map.Entry entry : entries) {
            final String mfaMethod = entry.getKey().toString();
            final String pattern = entry.getValue().toString();

            final Optional<MultifactorAuthenticationProvider> providerFound = resolveProvider(providerMap, mfaMethod);

            if (!providerFound.isPresent()) {
                LOGGER.error("Adaptive authentication is configured to require [{}] for [{}], yet [{}] is absent in the configuration.",
                            mfaMethod, pattern, mfaMethod);
                throw new AuthenticationException();
            }

            if (checkUserAgentOrClientIp(clientIp, agent, mfaMethod, pattern)) {
                return buildEvent(context, service, authentication, providerFound.get());
            }

            if (checkRequestGeoLocation(context, clientIp, mfaMethod, pattern)) {
                return buildEvent(context, service, authentication, providerFound.get());
            }
        }
        return null;
    }

    private boolean checkRequestGeoLocation(final RequestContext context, final String clientIp, final String mfaMethod, final String pattern) {
        if (this.geoLocationService != null) {
            final GeoLocationRequest location = WebUtils.getHttpServletRequestGeoLocationFromRequestContext(context);
            final GeoLocationResponse loc = this.geoLocationService.locate(clientIp, location);
            if (loc != null) {
                final String address = loc.build();
                if (address.matches(pattern)) {
                    LOGGER.debug("Current address [{}] at [{}] matches the provided pattern [{}] for "
                                    + "adaptive authentication and is required to use [{}]",
                            address, clientIp, pattern, mfaMethod);
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean checkUserAgentOrClientIp(final String clientIp, final String agent, final String mfaMethod, final String pattern) {
        if (agent.matches(pattern) || clientIp.matches(pattern)) {
            LOGGER.debug("Current user agent [{}] at [{}] matches the provided pattern [{}] for "
                         + "adaptive authentication and is required to use [{}]",
                        agent, clientIp, pattern, mfaMethod);
            return true;
        }
        return false;
    }

    private Set<Event> buildEvent(final RequestContext context, final RegisteredService service, 
                                  final Authentication authentication, 
                                  final MultifactorAuthenticationProvider provider) {
        LOGGER.debug("Attempting to build an event based on the authentication provider [{}] and service [{}]",
                provider, service.getName());
        final Event event = validateEventIdForMatchingTransitionInContext(provider.getId(), context,
                buildEventAttributeMap(authentication.getPrincipal(), service, provider));
        return CollectionUtils.wrapSet(event);
    }

    @Audit(action = "AUTHENTICATION_EVENT", 
            actionResolverName = "AUTHENTICATION_EVENT_ACTION_RESOLVER",
            resourceResolverName = "AUTHENTICATION_EVENT_RESOURCE_RESOLVER")
    @Override
    public Event resolveSingle(final RequestContext context) {
        return super.resolveSingle(context);
    }
}
