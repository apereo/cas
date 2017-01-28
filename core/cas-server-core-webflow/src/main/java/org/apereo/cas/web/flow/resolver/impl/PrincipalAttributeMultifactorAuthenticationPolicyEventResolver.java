package org.apereo.cas.web.flow.resolver.impl;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.validation.AuthenticationRequestServiceSelectionStrategy;
import org.apereo.cas.web.flow.authentication.BaseMultifactorAuthenticationProviderEventResolver;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.springframework.util.StringUtils.commaDelimitedListToSet;

/**
 * This is {@link PrincipalAttributeMultifactorAuthenticationPolicyEventResolver}
 * that attempts to locate a principal attribute, match its value against
 * the provided pattern and decide the next event in the flow for the given service.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class PrincipalAttributeMultifactorAuthenticationPolicyEventResolver extends BaseMultifactorAuthenticationProviderEventResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrincipalAttributeMultifactorAuthenticationPolicyEventResolver.class);
    
    private final String globalPrincipalAttributeValueRegex;
    private final Set<String> attributeNames;

    public PrincipalAttributeMultifactorAuthenticationPolicyEventResolver(final AuthenticationSystemSupport authenticationSystemSupport,
                                                                          final CentralAuthenticationService centralAuthenticationService,
                                                                          final ServicesManager servicesManager,
                                                                          final TicketRegistrySupport ticketRegistrySupport,
                                                                          final CookieGenerator warnCookieGenerator,
                                                                          final List<AuthenticationRequestServiceSelectionStrategy> authSelectionStrategies,
                                                                          final MultifactorAuthenticationProviderSelector selector,
                                                                          final CasConfigurationProperties casProperties) {
        super(authenticationSystemSupport, centralAuthenticationService, servicesManager, ticketRegistrySupport, warnCookieGenerator, authSelectionStrategies,
                selector);
        globalPrincipalAttributeValueRegex = casProperties.getAuthn().getMfa().getGlobalPrincipalAttributeValueRegex();
        attributeNames = commaDelimitedListToSet(casProperties.getAuthn().getMfa().getGlobalPrincipalAttributeNameTriggers());
    }

    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        final RegisteredService service = resolveRegisteredServiceInRequestContext(context);
        final Authentication authentication = WebUtils.getAuthentication(context);

        if (service == null || authentication == null) {
            LOGGER.debug("No service or authentication is available to determine event for principal");
            return null;
        }

        final Principal principal = authentication.getPrincipal();
        if (attributeNames.isEmpty()) {
            LOGGER.debug("Attribute name to determine event is not configured for [{}]", principal.getId());
            return null;
        }

        final Map<String, MultifactorAuthenticationProvider> providerMap =
                WebUtils.getAvailableMultifactorAuthenticationProviders(this.applicationContext);
        if (providerMap == null || providerMap.isEmpty()) {
            LOGGER.error("No multifactor authentication providers are available in the application context");
            return null;
        }

        final Collection<MultifactorAuthenticationProvider> providers = flattenProviders(providerMap.values());
        if (providers.size() == 1 && StringUtils.isNotBlank(globalPrincipalAttributeValueRegex)) {
            final MultifactorAuthenticationProvider provider = providers.iterator().next();
            LOGGER.debug("Found a single multifactor provider [{}] in the application context", provider);
            return resolveEventViaPrincipalAttribute(principal, attributeNames, service, context, providers,
                    input -> input != null && input.matches(globalPrincipalAttributeValueRegex));
        }

        return resolveEventViaPrincipalAttribute(principal, attributeNames, service, context, providers,
                input -> providers.stream()
                        .filter(provider -> input != null && provider.matches(input))
                        .count() > 0);
    }

    @Audit(action = "AUTHENTICATION_EVENT", actionResolverName = "AUTHENTICATION_EVENT_ACTION_RESOLVER",
            resourceResolverName = "AUTHENTICATION_EVENT_RESOURCE_RESOLVER")
    @Override
    public Event resolveSingle(final RequestContext context) {
        return super.resolveSingle(context);
    }
}
