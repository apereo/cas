package org.apereo.cas.web.flow.resolver.impl.mfa;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.authentication.BaseMultifactorAuthenticationProviderEventResolver;
import org.apereo.cas.web.support.WebUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.springframework.core.Ordered;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link RegisteredServiceMultifactorAuthenticationPolicyEventResolver}
 * that attempts to resolve the next event based on the authentication providers of this service.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@Getter
@Setter
public class RegisteredServiceMultifactorAuthenticationPolicyEventResolver extends BaseMultifactorAuthenticationProviderEventResolver implements MultifactorAuthenticationTrigger {
    private int order = Ordered.LOWEST_PRECEDENCE;

    public RegisteredServiceMultifactorAuthenticationPolicyEventResolver(final AuthenticationSystemSupport authenticationSystemSupport,
                                                                         final CentralAuthenticationService centralAuthenticationService,
                                                                         final ServicesManager servicesManager,
                                                                         final TicketRegistrySupport ticketRegistrySupport,
                                                                         final CookieGenerator warnCookieGenerator,
                                                                         final AuthenticationServiceSelectionPlan authSelectionStrategies,
                                                                         final MultifactorAuthenticationProviderSelector selector) {
        super(authenticationSystemSupport, centralAuthenticationService, servicesManager,
            ticketRegistrySupport, warnCookieGenerator, authSelectionStrategies, selector);
    }

    @Override
    public Optional<MultifactorAuthenticationProvider> isActivated(final Authentication authentication,
                                                                   final RegisteredService registeredService, final HttpServletRequest httpServletRequest, final Service service) {
        if (registeredService == null || authentication == null) {
            LOGGER.debug("No service or authentication is available to determine event for principal");
            return Optional.empty();
        }

        val policy = registeredService.getMultifactorPolicy();
        if (policy == null || policy.getMultifactorAuthenticationProviders().isEmpty()) {
            LOGGER.debug("Authentication policy does not contain any multifactor authentication providers");
            return Optional.empty();
        }

        if (StringUtils.isNotBlank(policy.getPrincipalAttributeNameTrigger()) || StringUtils.isNotBlank(policy.getPrincipalAttributeValueToMatch())) {
            LOGGER.debug("Authentication policy for [{}] has defined principal attribute triggers. Skipping...", registeredService.getServiceId());
            return Optional.empty();
        }

        val principal = authentication.getPrincipal();
        val providers = getAuthenticationProviderForService(registeredService);
        if (providers != null && !providers.isEmpty()) {
            val provider = this.multifactorAuthenticationProviderSelector.resolve(providers, registeredService, principal);
            LOGGER.debug("Selected multifactor authentication provider for this transaction is [{}]", provider);
            return Optional.of(provider);
        }

        return Optional.empty();
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

    @Audit(action = "AUTHENTICATION_EVENT",
        actionResolverName = "AUTHENTICATION_EVENT_ACTION_RESOLVER",
        resourceResolverName = "AUTHENTICATION_EVENT_RESOURCE_RESOLVER")
    @Override
    public Event resolveSingle(final RequestContext context) {
        return super.resolveSingle(context);
    }
}
