package org.apereo.cas.web.flow.resolver.impl.mfa.adaptive;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.authentication.TimeBasedAuthenticationProperties;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.authentication.BaseMultifactorAuthenticationProviderEventResolver;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.audit.annotation.Audit;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link TimedMultifactorAuthenticationPolicyEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class TimedMultifactorAuthenticationPolicyEventResolver extends BaseMultifactorAuthenticationProviderEventResolver {
    private final List<TimeBasedAuthenticationProperties> timedMultifactor;

    public TimedMultifactorAuthenticationPolicyEventResolver(final AuthenticationSystemSupport authenticationSystemSupport,
                                                             final CentralAuthenticationService centralAuthenticationService,
                                                             final ServicesManager servicesManager,
                                                             final TicketRegistrySupport ticketRegistrySupport,
                                                             final CookieGenerator warnCookieGenerator,
                                                             final AuthenticationServiceSelectionPlan authenticationSelectionStrategies,
                                                             final MultifactorAuthenticationProviderSelector selector,
                                                             final CasConfigurationProperties casProperties) {
        super(authenticationSystemSupport, centralAuthenticationService,
            servicesManager, ticketRegistrySupport, warnCookieGenerator,
            authenticationSelectionStrategies, selector);
        this.timedMultifactor = casProperties.getAuthn().getAdaptive().getRequireTimedMultifactor();
    }

    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        val service = resolveRegisteredServiceInRequestContext(context);
        val authentication = WebUtils.getAuthentication(context);

        if (service == null || authentication == null) {
            LOGGER.debug("No service or authentication is available to determine event for principal");
            return null;
        }

        if (timedMultifactor == null || timedMultifactor.isEmpty()) {
            LOGGER.debug("Adaptive authentication is not configured to require multifactor authentication by time");
            return null;
        }

        val providerMap =
            MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(this.applicationContext);
        if (providerMap == null || providerMap.isEmpty()) {
            LOGGER.error("No multifactor authentication providers are available in the application context");
            throw new AuthenticationException();
        }

        val providerFound = checkTimedMultifactorProvidersForRequest(context, service, authentication);
        if (providerFound != null && !providerFound.isEmpty()) {
            LOGGER.warn("Found multifactor authentication providers [{}] required for this authentication event", providerFound);
            return providerFound;
        }

        return null;
    }


    private Set<Event> checkTimedMultifactorProvidersForRequest(final RequestContext context, final RegisteredService service,
                                                                final Authentication authentication) {

        val now = LocalDateTime.now();
        val dow = DayOfWeek.from(now);
        val dayNamesForToday = Arrays.stream(TextStyle.values())
            .map(style -> dow.getDisplayName(style, Locale.getDefault()))
            .collect(Collectors.toList());

        val providerMap =
            MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(this.applicationContext);
        val timed = this.timedMultifactor.stream()
            .filter(t -> {
                var providerEvent = false;
                if (!t.getOnDays().isEmpty()) {
                    providerEvent = t.getOnDays().stream().anyMatch(dayNamesForToday::contains);
                }
                if (t.getOnOrAfterHour() >= 0) {
                    providerEvent = now.getHour() >= t.getOnOrAfterHour();
                }
                if (t.getOnOrBeforeHour() >= 0) {
                    providerEvent = now.getHour() <= t.getOnOrBeforeHour();
                }
                return providerEvent;
            })
            .findFirst()
            .orElse(null);

        if (timed != null) {
            val providerFound = resolveProvider(providerMap, timed.getProviderId());
            if (!providerFound.isPresent()) {
                LOGGER.error("Adaptive authentication is configured to require [{}] for [{}], yet [{}] absent in the configuration.",
                    timed.getProviderId(), service, timed.getProviderId());
                throw new AuthenticationException();
            }
            return buildEvent(context, service, authentication, providerFound.get());
        }
        return null;
    }

    private Set<Event> buildEvent(final RequestContext context, final RegisteredService service,
                                  final Authentication authentication,
                                  final MultifactorAuthenticationProvider provider) {
        if (provider.isAvailable(service)) {
            LOGGER.debug("Attempting to build an event based on the authentication provider [{}] and service [{}]",
                provider, service.getName());
            val event = validateEventIdForMatchingTransitionInContext(provider.getId(), context,
                buildEventAttributeMap(authentication.getPrincipal(), service, provider));
            return CollectionUtils.wrapSet(event);
        }
        LOGGER.warn("Located multifactor provider [{}], yet the provider cannot be reached or verified", provider);
        return null;
    }

    @Audit(action = "AUTHENTICATION_EVENT",
        actionResolverName = "AUTHENTICATION_EVENT_ACTION_RESOLVER",
        resourceResolverName = "AUTHENTICATION_EVENT_RESOURCE_RESOLVER")
    @Override
    public Event resolveSingle(final RequestContext context) {
        return super.resolveSingle(context);
    }
}
