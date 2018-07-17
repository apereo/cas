package org.apereo.cas.oidc.web.flow;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.authentication.BaseMultifactorAuthenticationProviderEventResolver;
import org.apereo.cas.web.support.WebUtils;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.jasig.cas.client.util.URIBuilder;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Set;

/**
 * This is {@link OidcAuthenticationContextWebflowEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class OidcAuthenticationContextWebflowEventResolver extends BaseMultifactorAuthenticationProviderEventResolver {


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
    public Set<Event> resolveInternal(final RequestContext context) {
        val service = resolveRegisteredServiceInRequestContext(context);
        val authentication = WebUtils.getAuthentication(context);
        val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(context);

        if (service == null || authentication == null) {
            LOGGER.debug("No service or authentication is available to determine event for principal");
            return null;
        }

        var acr = request.getParameter(OAuth20Constants.ACR_VALUES);
        if (StringUtils.isBlank(acr)) {
            val builderContext = new URIBuilder(StringUtils.trimToEmpty(context.getFlowExecutionUrl()));
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
            return null;
        }
        val values = org.springframework.util.StringUtils.commaDelimitedListToSet(acr);
        if (values.isEmpty()) {
            LOGGER.debug("No ACR provided in the authentication request");
            return null;
        }

        val providerMap =
            MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(this.applicationContext);
        if (providerMap == null || providerMap.isEmpty()) {
            LOGGER.error("No multifactor authentication providers are available in the application context to handle [{}]", values);
            throw new AuthenticationException();
        }

        val flattenedProviders = flattenProviders(providerMap.values());
        val provider = flattenedProviders
            .stream()
            .filter(v -> values.contains(v.getId()))
            .findAny();

        if (provider.isPresent()) {
            return CollectionUtils.wrapSet(new Event(this, provider.get().getId()));
        }
        LOGGER.warn("The requested authentication class [{}] cannot be satisfied by any of the MFA providers available", values);
        throw new AuthenticationException();
    }
}
