package org.apereo.cas.web.flow.resolver.impl.mfa;

import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.MultifactorAuthenticationProvider;
import org.apereo.cas.services.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.flow.authentication.BaseMultifactorAuthenticationProviderEventResolver;
import org.apereo.cas.web.support.WebUtils;
import org.apereo.inspektr.audit.annotation.Audit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Collection;
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

    /**
     * Principal attribute value regex.
     */
    protected final String globalPrincipalAttributeValueRegex;

    /**
     * Principal attribute names.
     */
    protected final Set<String> attributeNames;

    public PrincipalAttributeMultifactorAuthenticationPolicyEventResolver(final AuthenticationSystemSupport authenticationSystemSupport,
                                                                          final CentralAuthenticationService centralAuthenticationService,
                                                                          final ServicesManager servicesManager,
                                                                          final TicketRegistrySupport ticketRegistrySupport,
                                                                          final CookieGenerator warnCookieGenerator,
                                                                          final AuthenticationServiceSelectionPlan authSelectionStrategies,
                                                                          final MultifactorAuthenticationProviderSelector selector,
                                                                          final CasConfigurationProperties casProperties) {
        super(authenticationSystemSupport, centralAuthenticationService, servicesManager,
            ticketRegistrySupport, warnCookieGenerator, authSelectionStrategies, selector);
        globalPrincipalAttributeValueRegex = casProperties.getAuthn().getMfa().getGlobalPrincipalAttributeValueRegex();
        attributeNames = commaDelimitedListToSet(casProperties.getAuthn().getMfa().getGlobalPrincipalAttributeNameTriggers());
    }

    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        final RegisteredService service = resolveRegisteredServiceInRequestContext(context);
        final Authentication authentication = WebUtils.getAuthentication(context);

        if (authentication == null) {
            LOGGER.debug("No service or authentication is available to determine event for principal");
            return null;
        }


        final Principal principal = authentication.getPrincipal();
        return resolveMultifactorAuthenticationProvider(context, service, principal);
    }

    /**
     * Resolve multifactor authentication provider set.
     *
     * @param context   the context
     * @param service   the service
     * @param principal the principal
     * @return the set
     */
    protected Set<Event> resolveMultifactorAuthenticationProvider(final RequestContext context, final RegisteredService service,
                                                                  final Principal principal) {
        final Map<String, MultifactorAuthenticationProvider> providerMap =
            MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(this.applicationContext);
        final Collection<MultifactorAuthenticationProvider> providers = flattenProviders(providerMap.values());
        if (providers.size() == 1 && StringUtils.isNotBlank(globalPrincipalAttributeValueRegex)) {
            return resolveSingleMultifactorProvider(context, service, principal, providers);
        }

        return resolveMultifactorProviderViaPredicate(context, service, principal, providers);
    }

    /**
     * Resolve multifactor provider by regex predicate set.
     *
     * @param context   the context
     * @param service   the service
     * @param principal the principal
     * @param providers the providers
     * @return the set
     */
    protected Set<Event> resolveMultifactorProviderViaPredicate(final RequestContext context,
                                                                final RegisteredService service,
                                                                final Principal principal,
                                                                final Collection<MultifactorAuthenticationProvider> providers) {
        return resolveEventViaPrincipalAttribute(principal, attributeNames, service, context, providers,
            input -> providers.stream()
                .filter(provider -> input != null && provider.matches(input))
                .count() > 0);
    }

    /**
     * Resolve single multifactor provider set.
     *
     * @param context   the context
     * @param service   the service
     * @param principal the principal
     * @param providers the providers
     * @return the set
     */
    protected Set<Event> resolveSingleMultifactorProvider(final RequestContext context, final RegisteredService service,
                                                          final Principal principal,
                                                          final Collection<MultifactorAuthenticationProvider> providers) {
        final MultifactorAuthenticationProvider provider = providers.iterator().next();
        LOGGER.debug("Found a single multifactor provider [{}] in the application context", provider);
        return resolveEventViaPrincipalAttribute(principal, attributeNames, service, context, providers,
            input -> input != null && input.matches(globalPrincipalAttributeValueRegex));
    }

    @Audit(action = "AUTHENTICATION_EVENT",
        actionResolverName = "AUTHENTICATION_EVENT_ACTION_RESOLVER",
        resourceResolverName = "AUTHENTICATION_EVENT_RESOURCE_RESOLVER")
    @Override
    public Event resolveSingle(final RequestContext context) {
        return super.resolveSingle(context);
    }
}
