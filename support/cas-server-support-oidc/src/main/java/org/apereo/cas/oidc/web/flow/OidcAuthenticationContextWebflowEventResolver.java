package org.apereo.cas.oidc.web.flow;

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
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.authentication.BaseMultifactorAuthenticationProviderEventResolver;
import org.apereo.cas.web.support.WebUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.client.util.URIBuilder;
import org.springframework.core.Ordered;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.Set;

/**
 * This is {@link OidcAuthenticationContextWebflowEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@Getter
@Setter
public class OidcAuthenticationContextWebflowEventResolver extends BaseMultifactorAuthenticationProviderEventResolver implements MultifactorAuthenticationTrigger {

    private int order = Ordered.LOWEST_PRECEDENCE;

    public OidcAuthenticationContextWebflowEventResolver(final AuthenticationSystemSupport authenticationSystemSupport,
                                                         final CentralAuthenticationService centralAuthenticationService,
                                                         final ServicesManager servicesManager,
                                                         final TicketRegistrySupport ticketRegistrySupport,
                                                         final CookieGenerator warnCookieGenerator,
                                                         final AuthenticationServiceSelectionPlan authenticationSelectionStrategies,
                                                         final MultifactorAuthenticationProviderSelector selector) {
        super(authenticationSystemSupport, centralAuthenticationService, servicesManager,
            ticketRegistrySupport, warnCookieGenerator,
            authenticationSelectionStrategies, selector);
    }

    @Override
    public Optional<MultifactorAuthenticationProvider> isActivated(final Authentication authentication,
                                                                   final RegisteredService registeredService, final HttpServletRequest request, final Service service) {
        if (registeredService == null || authentication == null) {
            LOGGER.debug("No service or authentication is available to determine event for principal");
            return Optional.empty();
        }

        var acr = request.getParameter(OAuth20Constants.ACR_VALUES);
        if (StringUtils.isBlank(acr)) {
            val builderContext = new URIBuilder(request.getRequestURI());
            val parameter = builderContext.getQueryParams()
                .stream()
                .filter(p -> p.getName().equals(OAuth20Constants.ACR_VALUES))
                .findFirst();
            if (parameter.isPresent()) {
                acr = parameter.get().getValue();
            }
        }
        if (StringUtils.isBlank(acr)) {
            LOGGER.debug("No ACR provided in the authentication request");
            return Optional.empty();
        }
        val values = org.springframework.util.StringUtils.commaDelimitedListToSet(acr);
        if (values.isEmpty()) {
            LOGGER.debug("No ACR provided in the authentication request");
            return Optional.empty();
        }

        val providerMap = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(this.applicationContext);
        if (providerMap.isEmpty()) {
            LOGGER.error("No multifactor authentication providers are available in the application context to handle [{}]", values);
            throw new AuthenticationException();
        }

        return providerMap.values()
            .stream()
            .filter(v -> values.contains(v.getId()))
            .findAny();
    }


    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        val registeredService = resolveRegisteredServiceInRequestContext(context);
        val service = resolveServiceFromAuthenticationRequest(context);
        val authentication = WebUtils.getAuthentication(context);
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);

        val result = isActivated(authentication, registeredService, request, service);

        if (result.isPresent()) {
            return CollectionUtils.wrapSet(new Event(this, result.get().getId()));
        }
        LOGGER.warn("The requested authentication class cannot be satisfied by any of the MFA providers available");
        throw new AuthenticationException();
    }
}
