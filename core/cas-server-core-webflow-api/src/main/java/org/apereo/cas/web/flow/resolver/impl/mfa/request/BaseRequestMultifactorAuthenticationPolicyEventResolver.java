package org.apereo.cas.web.flow.resolver.impl.mfa.request;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
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
import java.util.List;
import java.util.Set;

/**
 * This is {@link BaseRequestMultifactorAuthenticationPolicyEventResolver}
 * that attempts to resolve the next event based on the authentication providers of this service.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public abstract class BaseRequestMultifactorAuthenticationPolicyEventResolver extends BaseMultifactorAuthenticationProviderEventResolver {


    public BaseRequestMultifactorAuthenticationPolicyEventResolver(final AuthenticationSystemSupport authenticationSystemSupport,
                                                                   final CentralAuthenticationService centralAuthenticationService,
                                                                   final ServicesManager servicesManager,
                                                                   final TicketRegistrySupport ticketRegistrySupport,
                                                                   final CookieGenerator warnCookieGenerator,
                                                                   final AuthenticationServiceSelectionPlan authenticationStrategies,
                                                                   final MultifactorAuthenticationProviderSelector selector,
                                                                   final CasConfigurationProperties casProperties) {
        super(authenticationSystemSupport, centralAuthenticationService, servicesManager,
            ticketRegistrySupport, warnCookieGenerator, authenticationStrategies, selector);
    }

    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        val service = resolveRegisteredServiceInRequestContext(context);
        val authentication = WebUtils.getAuthentication(context);

        if (service == null || authentication == null) {
            LOGGER.debug("No service or authentication is available to determine event for principal");
            return null;
        }

        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        val values = resolveEventFromHttpRequest(request);
        if (values != null && !values.isEmpty()) {
            LOGGER.debug("Received request as [{}]", values);

            val providerMap = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(this.applicationContext);
            if (providerMap == null || providerMap.isEmpty()) {
                LOGGER.error("No multifactor authentication providers are available in the application context to satisfy [{}]", values);
                throw new AuthenticationException();
            }

            val providerFound = resolveProvider(providerMap, values.get(0));
            if (providerFound.isPresent()) {
                val provider = providerFound.get();
                if (provider.isAvailable(service)) {
                    LOGGER.debug("Attempting to build an event based on the authentication provider [{}] and service [{}]", provider, service.getName());
                    val event = validateEventIdForMatchingTransitionInContext(provider.getId(), context,
                        buildEventAttributeMap(authentication.getPrincipal(), service, provider));
                    return CollectionUtils.wrapSet(event);
                }
                LOGGER.warn("Located multifactor provider [{}], yet the provider cannot be reached or verified", providerFound.get());
                return null;
            }
            LOGGER.warn("No multifactor provider could be found for request parameter [{}]", values);
            throw new AuthenticationException();
        }
        return null;
    }

    @Audit(action = "AUTHENTICATION_EVENT",
        actionResolverName = "AUTHENTICATION_EVENT_ACTION_RESOLVER",
        resourceResolverName = "AUTHENTICATION_EVENT_RESOURCE_RESOLVER")
    @Override
    public Event resolveSingle(final RequestContext context) {
        return super.resolveSingle(context);
    }

    /**
     * Resolve event from http request.
     *
     * @param request the request
     * @return the list
     */
    protected abstract List<String> resolveEventFromHttpRequest(HttpServletRequest request);
}
