package org.apereo.cas.web.flow.authentication;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.AuthenticationCredentialsThreadLocalBinder;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.RegisteredServiceAuthenticationPolicy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
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

    private final ConfigurableApplicationContext applicationContext;

    private final AuthenticationEventExecutionPlan authenticationEventExecutionPlan;

    @Override
    public boolean isParticipating(final RequestContext requestContext) {
        val registeredService = determineRegisteredService(requestContext);
        if (registeredService == null) {
            return true;
        }
        val authenticationPolicy = registeredService.getAuthenticationPolicy();
        if (authenticationPolicy == null || authenticationPolicy.getRequiredAuthenticationHandlers().isEmpty()) {
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
            return verifyRegisteredServiceAuthenticationPolicy(authenticationPolicy, authentication, registeredService);
        } finally {
            AuthenticationCredentialsThreadLocalBinder.bindCurrent(ca);
        }
    }

    @Override
    public boolean supports(final RequestContext requestContext) {
        val registeredService = determineRegisteredService(requestContext);
        if (registeredService == null) {
            return false;
        }
        val authenticationPolicy = registeredService.getAuthenticationPolicy();
        if (authenticationPolicy == null || authenticationPolicy.getRequiredAuthenticationHandlers().isEmpty()) {
            return false;
        }
        return true;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private boolean verifyRegisteredServiceAuthenticationPolicy(final RegisteredServiceAuthenticationPolicy authenticationPolicy,
                                                                final Authentication authentication,
                                                                final RegisteredService registeredService) {
        try {
            val handlerNames = authentication.getAttributes().get(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS);
            LOGGER.trace("Successful authentication handlers for this transaction are [{}]", handlerNames);

            val policyCriteria = authenticationPolicy.getCriteria();
            val serviceAuthnPolicy = policyCriteria.toAuthenticationPolicy();

            val handlers = authenticationEventExecutionPlan.getAuthenticationHandlersBy(
                handler -> handlerNames.contains(handler.getName()));

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Filtered authentication handlers for authentication policy evaluation are [{}]",
                    handlers.stream().map(AuthenticationHandler::getName).collect(Collectors.joining(",")));
            }
            if (serviceAuthnPolicy.isSatisfiedBy(authentication, handlers, applicationContext, Optional.empty())) {
                LOGGER.debug("Authentication policy [{}] is satisfied for registered service [{}]",
                    authenticationPolicy, registeredService.getName());
                return true;
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        LOGGER.warn("Authentication policy [{}] cannot be established/satisfied for registered service [{}]",
            authenticationPolicy, registeredService.getName());
        return false;
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
