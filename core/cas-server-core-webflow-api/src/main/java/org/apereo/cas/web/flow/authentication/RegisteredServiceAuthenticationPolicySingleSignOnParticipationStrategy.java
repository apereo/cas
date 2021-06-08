package org.apereo.cas.web.flow.authentication;

import org.apereo.cas.authentication.AuthenticationCredentialsThreadLocalBinder;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.execution.RequestContext;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link RegisteredServiceAuthenticationPolicySingleSignOnParticipationStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiredArgsConstructor
@Slf4j
public class RegisteredServiceAuthenticationPolicySingleSignOnParticipationStrategy
    implements SingleSignOnParticipationStrategy {

    private final ServicesManager servicesManager;

    private final AuthenticationServiceSelectionPlan serviceSelectionStrategy;

    private final TicketRegistrySupport ticketRegistrySupport;

    private final AuthenticationEventExecutionPlan authenticationEventExecutionPlan;

    private final ConfigurableApplicationContext applicationContext;

    @Override
    @SneakyThrows
    public boolean isParticipating(final RequestContext requestContext) {
        val registeredService = determineRegisteredService(requestContext);
        if (registeredService == null) {
            return true;
        }
        val authenticationPolicy = registeredService.getAuthenticationPolicy();
        if (authenticationPolicy == null) {
            return true;
        }

        val ticketGrantingTicketId = WebUtils.getTicketGrantingTicketId(requestContext);
        if (StringUtils.isBlank(ticketGrantingTicketId)) {
            return true;
        }
        val authentication = ticketRegistrySupport.getAuthenticationFrom(ticketGrantingTicketId);
        val ca = AuthenticationCredentialsThreadLocalBinder.getCurrentAuthentication();
        try {
            AuthenticationCredentialsThreadLocalBinder.bindCurrent(authentication);
            if (authentication != null) {
                val successfulHandlerNames = CollectionUtils.toCollection(authentication.getAttributes()
                    .get(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS));
                val assertedHandlers = authenticationEventExecutionPlan.getAuthenticationHandlers()
                    .stream()
                    .filter(handler -> successfulHandlerNames.contains(handler.getName()))
                    .collect(Collectors.toSet());
                LOGGER.debug("Asserted authentication handlers are [{}]", assertedHandlers);
                val criteria = authenticationPolicy.getCriteria();
                if (criteria != null) {
                    val policy = criteria.toAuthenticationPolicy(registeredService);
                    LOGGER.debug("Authentication policy to satisfy is [{}]", policy);
                    return policy.isSatisfiedBy(authentication, assertedHandlers, applicationContext, Optional.empty());
                }
            }
        } finally {
            AuthenticationCredentialsThreadLocalBinder.bindCurrent(ca);
        }
        return true;
    }

    @Override
    public boolean supports(final RequestContext requestContext) {
        val registeredService = determineRegisteredService(requestContext);
        if (registeredService == null) {
            return false;
        }
        val authenticationPolicy = registeredService.getAuthenticationPolicy();
        LOGGER.debug("Evaluating authentication policy [{}] for [{}]", authenticationPolicy, registeredService.getName());
        return authenticationPolicy != null && authenticationPolicy.getCriteria() != null;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private RegisteredService determineRegisteredService(final RequestContext requestContext) {
        val registeredService = WebUtils.getRegisteredService(requestContext);
        if (registeredService != null) {
            return registeredService;
        }
        val service = WebUtils.getService(requestContext);
        val serviceToUse = serviceSelectionStrategy.resolveService(service);
        if (serviceToUse != null) {
            return this.servicesManager.findServiceBy(serviceToUse);
        }
        return null;
    }
}
