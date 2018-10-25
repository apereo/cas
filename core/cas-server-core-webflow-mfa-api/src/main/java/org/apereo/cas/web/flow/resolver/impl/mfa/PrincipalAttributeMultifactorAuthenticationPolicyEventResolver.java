package org.apereo.cas.web.flow.resolver.impl.mfa;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.MultifactorAuthenticationProvider;
import org.apereo.cas.authentication.MultifactorAuthenticationProviderSelector;
import org.apereo.cas.authentication.MultifactorAuthenticationTrigger;
import org.apereo.cas.authentication.MultifactorAuthenticationUtils;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.configuration.CasConfigurationProperties;
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
import java.util.Collection;
import java.util.Optional;
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
@Slf4j
@Getter
@Setter
public class PrincipalAttributeMultifactorAuthenticationPolicyEventResolver extends BaseMultifactorAuthenticationProviderEventResolver
    implements MultifactorAuthenticationTrigger {
    /**
     * Principal attribute value regex.
     */
    protected final String globalPrincipalAttributeValueRegex;

    /**
     * Principal attribute names.
     */
    protected final Set<String> attributeNames;

    private int order = Ordered.LOWEST_PRECEDENCE;

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
    public Optional<MultifactorAuthenticationProvider> isActivated(final Authentication authentication,
                                                                   final RegisteredService registeredService, final HttpServletRequest httpServletRequest, final Service service) {
        if (authentication == null) {
            LOGGER.debug("No authentication is available to determine event for principal");
            return Optional.empty();
        }

        val result = resolveMultifactorAuthenticationProvider(Optional.empty(), registeredService, authentication.getPrincipal());
        if (result != null && !result.isEmpty()) {
            val id = CollectionUtils.firstElement(result);
            return MultifactorAuthenticationUtils.getMultifactorAuthenticationProviderById(id.toString(), applicationContext);
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

    /**
     * Resolve multifactor authentication provider set.
     *
     * @param context   the context
     * @param service   the service
     * @param principal the principal
     * @return the set
     */
    protected Set<Event> resolveMultifactorAuthenticationProvider(final Optional<RequestContext> context, final RegisteredService service,
                                                                  final Principal principal) {
        val providerMap = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(this.applicationContext);
        val providers = providerMap.values();
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
    protected Set<Event> resolveMultifactorProviderViaPredicate(final Optional<RequestContext> context,
                                                                final RegisteredService service,
                                                                final Principal principal,
                                                                final Collection<MultifactorAuthenticationProvider> providers) {
        return resolveEventViaPrincipalAttribute(principal, attributeNames, service, context, providers,
            input -> providers.stream().anyMatch(provider -> input != null && provider.matches(input)));
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
    protected Set<Event> resolveSingleMultifactorProvider(final Optional<RequestContext> context, final RegisteredService service,
                                                          final Principal principal,
                                                          final Collection<MultifactorAuthenticationProvider> providers) {
        val provider = providers.iterator().next();
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
