package org.apereo.cas.web.flow.resolver.impl.mfa.adaptive;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.core.authentication.TimeBasedAuthenticationProperties;
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

import javax.servlet.http.HttpServletRequest;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is {@link TimedMultifactorAuthenticationPolicyEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
public class TimedMultifactorAuthenticationPolicyEventResolver extends BaseMultifactorAuthenticationProviderEventResolver
    implements MultifactorAuthenticationTrigger {
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
    public Optional<MultifactorAuthenticationProvider> isActivated(final Authentication authentication, final RegisteredService registeredService,
                                                                   final HttpServletRequest httpServletRequest, final Service service) {

        if (service == null || authentication == null) {
            LOGGER.debug("No service or authentication is available to determine event for principal");
            return Optional.empty();
        }

        if (timedMultifactor == null || timedMultifactor.isEmpty()) {
            LOGGER.debug("Adaptive authentication is not configured to require multifactor authentication by time");
            return Optional.empty();
        }

        val providerMap = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(this.applicationContext);
        if (providerMap.isEmpty()) {
            LOGGER.error("No multifactor authentication providers are available in the application context");
            throw new AuthenticationException();
        }

        return checkTimedMultifactorProvidersForRequest(registeredService, authentication);
    }

    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        val registeredService = resolveRegisteredServiceInRequestContext(context);
        val service = resolveServiceFromAuthenticationRequest(context);
        val authentication = WebUtils.getAuthentication(context);
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);

        val result = isActivated(authentication, registeredService, request, service);
        return result.map(provider -> {
            LOGGER.debug("Attempting to build an event based on the authentication provider [{}] and service [{}]", provider, registeredService.getName());
            val event = validateEventIdForMatchingTransitionInContext(provider.getId(), Optional.of(context),
                buildEventAttributeMap(authentication.getPrincipal(), Optional.of(registeredService), provider));
            return CollectionUtils.wrapSet(event);
        }).orElse(null);
    }


    private Optional<MultifactorAuthenticationProvider> checkTimedMultifactorProvidersForRequest(final RegisteredService service, final Authentication authentication) {

        val now = LocalDateTime.now();
        val dow = DayOfWeek.from(now);
        val dayNamesForToday = Arrays.stream(TextStyle.values())
            .map(style -> dow.getDisplayName(style, Locale.getDefault()))
            .collect(Collectors.toList());

        val providerMap = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(this.applicationContext);
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
            if (providerFound.isEmpty()) {
                LOGGER.error("Adaptive authentication is configured to require [{}] for [{}], yet [{}] absent in the configuration.",
                    timed.getProviderId(), service, timed.getProviderId());
                throw new AuthenticationException();
            }
            return providerFound;
        }
        return Optional.empty();
    }


    @Audit(action = "AUTHENTICATION_EVENT",
        actionResolverName = "AUTHENTICATION_EVENT_ACTION_RESOLVER",
        resourceResolverName = "AUTHENTICATION_EVENT_RESOURCE_RESOLVER")
    @Override
    public Event resolveSingle(final RequestContext context) {
        return super.resolveSingle(context);
    }
}
