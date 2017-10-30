package org.apereo.cas.web.flow.resolver.impl.mfa;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link RequestParameterMultifactorAuthenticationPolicyEventResolver}
 * that attempts to resolve the next event based on the authentication providers of this service.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class RequestParameterMultifactorAuthenticationPolicyEventResolver extends BaseMultifactorAuthenticationProviderEventResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestParameterMultifactorAuthenticationPolicyEventResolver.class);

    private final String mfaRequestParameter;

    public RequestParameterMultifactorAuthenticationPolicyEventResolver(final AuthenticationSystemSupport authenticationSystemSupport,
                                                                        final CentralAuthenticationService centralAuthenticationService,
                                                                        final ServicesManager servicesManager,
                                                                        final TicketRegistrySupport ticketRegistrySupport,
                                                                        final CookieGenerator warnCookieGenerator,
                                                                        final AuthenticationServiceSelectionPlan authenticationStrategies,
                                                                        final MultifactorAuthenticationProviderSelector selector,
                                                                        final CasConfigurationProperties casProperties) {
        super(authenticationSystemSupport, centralAuthenticationService, servicesManager,
                ticketRegistrySupport, warnCookieGenerator, authenticationStrategies, selector);
        mfaRequestParameter = casProperties.getAuthn().getMfa().getRequestParameter();
    }

    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        final RegisteredService service = resolveRegisteredServiceInRequestContext(context);
        final Authentication authentication = WebUtils.getAuthentication(context);

        if (service == null || authentication == null) {
            LOGGER.debug("No service or authentication is available to determine event for principal");
            return null;
        }
        if (StringUtils.isBlank(mfaRequestParameter)) {
            LOGGER.debug("No request parameter is defined to trigger multifactor authentication.");
            return null;
        }
        
        final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);
        final String[] values = request.getParameterValues(mfaRequestParameter);
        if (values != null && values.length > 0) {
            LOGGER.debug("Received request parameter [{}] as [{}]", mfaRequestParameter, values);

            final Map<String, MultifactorAuthenticationProvider> providerMap =
                    MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(this.applicationContext);
            if (providerMap == null || providerMap.isEmpty()) {
                LOGGER.error("No multifactor authentication providers are available in the application context to satisfy [{}]", (Object[]) values);
                throw new AuthenticationException();
            }

            final Optional<MultifactorAuthenticationProvider> providerFound = resolveProvider(providerMap, values[0]);
            if (providerFound.isPresent()) {
                final MultifactorAuthenticationProvider provider = providerFound.get();
                if (provider.isAvailable(service)) {
                    LOGGER.debug("Attempting to build an event based on the authentication provider [{}] and service [{}]", provider, service.getName());
                    final Event event = validateEventIdForMatchingTransitionInContext(provider.getId(), context,
                            buildEventAttributeMap(authentication.getPrincipal(), service, provider));
                    return CollectionUtils.wrapSet(event);
                }
                LOGGER.warn("Located multifactor provider [{}], yet the provider cannot be reached or verified", providerFound.get());
                return null;
            }
            LOGGER.warn("No multifactor provider could be found for request parameter [{}]", (Object[]) values);
            throw new AuthenticationException();
        }
        LOGGER.debug("No value could be found for request parameter [{}]", mfaRequestParameter);
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
