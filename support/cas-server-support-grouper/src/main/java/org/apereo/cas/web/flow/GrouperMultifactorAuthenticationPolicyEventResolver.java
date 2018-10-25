package org.apereo.cas.web.flow;

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
import org.apereo.cas.grouper.GrouperFacade;
import org.apereo.cas.grouper.GrouperGroupField;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.authentication.BaseMultifactorAuthenticationProviderEventResolver;
import org.apereo.cas.web.support.WebUtils;

import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.inspektr.audit.annotation.Audit;
import org.springframework.core.Ordered;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.servlet.http.HttpServletRequest;
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
@Slf4j
@Getter
@Setter
public class GrouperMultifactorAuthenticationPolicyEventResolver extends BaseMultifactorAuthenticationProviderEventResolver implements MultifactorAuthenticationTrigger {
    private final String grouperField;
    private final GrouperFacade grouperFacade;
    private int order = Ordered.LOWEST_PRECEDENCE;

    public GrouperMultifactorAuthenticationPolicyEventResolver(final AuthenticationSystemSupport authenticationSystemSupport,
                                                               final CentralAuthenticationService centralAuthenticationService,
                                                               final ServicesManager servicesManager,
                                                               final TicketRegistrySupport ticketRegistrySupport,
                                                               final CookieGenerator warnCookieGenerator,
                                                               final AuthenticationServiceSelectionPlan authenticationSelectionStrategies,
                                                               final MultifactorAuthenticationProviderSelector selector,
                                                               final CasConfigurationProperties casProperties,
                                                               final GrouperFacade grouperFacade) {
        super(authenticationSystemSupport, centralAuthenticationService, servicesManager, ticketRegistrySupport, warnCookieGenerator,
            authenticationSelectionStrategies, selector);
        grouperField = casProperties.getAuthn().getMfa().getGrouperGroupField().toUpperCase();
        this.grouperFacade = grouperFacade;
    }

    @Override
    public Optional<MultifactorAuthenticationProvider> isActivated(final Authentication authentication,
                                                                   final RegisteredService registeredService, final HttpServletRequest request, final Service service) {
        if (StringUtils.isBlank(grouperField)) {
            LOGGER.debug("No group field is defined to process for Grouper multifactor trigger");
            return Optional.empty();
        }
        if (authentication == null || registeredService == null) {
            LOGGER.debug("No authentication or service is available to determine event for principal");
            return Optional.empty();
        }

        val principal = authentication.getPrincipal();
        val results = grouperFacade.getGroupsForSubjectId(principal.getId());
        if (results.isEmpty()) {
            LOGGER.debug("No groups could be found for [{}] to resolve events for MFA", principal);
            return Optional.empty();
        }

        val providerMap = MultifactorAuthenticationUtils.getAvailableMultifactorAuthenticationProviders(this.applicationContext);
        if (providerMap.isEmpty()) {
            LOGGER.error("No multifactor authentication providers are available in the application context");
            throw new AuthenticationException();
        }

        val groupField = GrouperGroupField.valueOf(grouperField);

        val values = results.stream()
            .map(wsGetGroupsResult -> Stream.of(wsGetGroupsResult.getWsGroups()))
            .flatMap(Function.identity())
            .map(g -> GrouperFacade.getGrouperGroupAttribute(groupField, g))
            .collect(Collectors.toSet());

        return resolveProvider(providerMap, values);
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
