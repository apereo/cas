package org.apereo.cas.web.flow;

import edu.internet2.middleware.grouperClient.ws.beans.WsGetGroupsResult;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationException;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.authentication.AuthenticationSystemSupport;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.grouper.GrouperFacade;
import org.apereo.cas.grouper.GrouperGroupField;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link GrouperMultifactorAuthenticationPolicyEventResolver}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class GrouperMultifactorAuthenticationPolicyEventResolver extends BaseMultifactorAuthenticationProviderEventResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrouperMultifactorAuthenticationPolicyEventResolver.class);
    
    private final String grouperField;

    public GrouperMultifactorAuthenticationPolicyEventResolver(final AuthenticationSystemSupport authenticationSystemSupport,
                                                               final CentralAuthenticationService centralAuthenticationService,
                                                               final ServicesManager servicesManager, final TicketRegistrySupport ticketRegistrySupport,
                                                               final CookieGenerator warnCookieGenerator,
                                                               final AuthenticationServiceSelectionPlan authenticationSelectionStrategies,
                                                               final MultifactorAuthenticationProviderSelector selector,
                                                               final CasConfigurationProperties casProperties) {
        super(authenticationSystemSupport, centralAuthenticationService, servicesManager, ticketRegistrySupport, warnCookieGenerator,
                authenticationSelectionStrategies, selector);
        grouperField = casProperties.getAuthn().getMfa().getGrouperGroupField().toUpperCase();
    }

    @Override
    public Set<Event> resolveInternal(final RequestContext context) {
        final RegisteredService service = resolveRegisteredServiceInRequestContext(context);
        final Authentication authentication = WebUtils.getAuthentication(context);

        if (StringUtils.isBlank(grouperField)) {
            LOGGER.debug("No group field is defined to process for Grouper multifactor trigger");
            return null;
        }
        if (authentication == null) {
            LOGGER.debug("No authentication is available to determine event for principal");
            return null;
        }

        final Principal principal = authentication.getPrincipal();
        final List<WsGetGroupsResult> results = GrouperFacade.getGroupsForSubjectId(principal.getId());
        if (results.isEmpty()) {
            LOGGER.debug("No groups could be found for [{}] to resolve events for MFA", principal);
            return null;
        }

        final Map<String, MultifactorAuthenticationProvider> providerMap =
                WebUtils.getAvailableMultifactorAuthenticationProviders(this.applicationContext);
        if (providerMap == null || providerMap.isEmpty()) {
            LOGGER.error("No multifactor authentication providers are available in the application context");
            throw new AuthenticationException();
        }

        final GrouperGroupField groupField = GrouperGroupField.valueOf(grouperField);

        final Set<String> values = results.stream()
                .map(wsGetGroupsResult -> Stream.of(wsGetGroupsResult.getWsGroups()))
                .flatMap(Function.identity())
                .map(g -> GrouperFacade.getGrouperGroupAttribute(groupField, g))
                .collect(Collectors.toSet());

        final Optional<MultifactorAuthenticationProvider> providerFound = resolveProvider(providerMap, values);

        if (providerFound.isPresent()) {
            if (providerFound.get().isAvailable(service)) {
                LOGGER.debug("Attempting to build event based on the authentication provider [{}] and service [{}]",
                        providerFound.get(), service.getName());
                final Event event = validateEventIdForMatchingTransitionInContext(providerFound.get().getId(), context,
                        buildEventAttributeMap(authentication.getPrincipal(), service, providerFound.get()));
                return Collections.singleton(event);
            }
            LOGGER.warn("Located multifactor provider [{}], yet the provider cannot be reached or verified", providerFound.get());
            return null;
        }
        LOGGER.debug("No multifactor provider could be found based on [{}]'s Grouper groups", principal.getId());
        return null;
    }

    @Audit(action = "AUTHENTICATION_EVENT", actionResolverName = "AUTHENTICATION_EVENT_ACTION_RESOLVER",
            resourceResolverName = "AUTHENTICATION_EVENT_RESOURCE_RESOLVER")
    @Override
    public Event resolveSingle(final RequestContext context) {
        return super.resolveSingle(context);
    }
}
