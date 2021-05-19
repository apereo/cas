package org.apereo.cas.web.flow.authentication;

import org.apereo.cas.authentication.AuthenticationCredentialsThreadLocalBinder;
import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TicketState;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.BaseSingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.SingleSignOnParticipationRequest;

import lombok.SneakyThrows;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link RegisteredServiceAuthenticationPolicySingleSignOnParticipationStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
public class RegisteredServiceAuthenticationPolicySingleSignOnParticipationStrategy extends BaseSingleSignOnParticipationStrategy {

    private final AuthenticationEventExecutionPlan authenticationEventExecutionPlan;

    private final ConfigurableApplicationContext applicationContext;

    public RegisteredServiceAuthenticationPolicySingleSignOnParticipationStrategy(final ServicesManager servicesManager,
                                                                                  final TicketRegistrySupport ticketRegistrySupport,
                                                                                  final AuthenticationServiceSelectionPlan serviceSelectionStrategy,
                                                                                  final AuthenticationEventExecutionPlan authenticationEventExecutionPlan,
                                                                                  final ConfigurableApplicationContext applicationContext) {
        super(servicesManager, ticketRegistrySupport, serviceSelectionStrategy);
        this.authenticationEventExecutionPlan = authenticationEventExecutionPlan;
        this.applicationContext = applicationContext;
    }

    @Override
    @SneakyThrows
    public boolean isParticipating(final SingleSignOnParticipationRequest ssoRequest) {
        val registeredService = getRegisteredService(ssoRequest);
        if (registeredService == null) {
            return true;
        }
        val authenticationPolicy = registeredService.getAuthenticationPolicy();
        if (authenticationPolicy == null) {
            return true;
        }

        val ticketGrantingTicketId = getTicketGrantingTicketId(ssoRequest);
        if (ticketGrantingTicketId.isEmpty()) {
            return true;
        }

        val ca = AuthenticationCredentialsThreadLocalBinder.getCurrentAuthentication();
        try {
            val authentication = getTicketState(ssoRequest).map(TicketState::getAuthentication).orElseThrow();
            AuthenticationCredentialsThreadLocalBinder.bindCurrent(authentication);
            if (authentication != null) {
                val successfulHandlerNames = CollectionUtils.toCollection(authentication.getAttributes()
                    .get(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS));
                val assertedHandlers = authenticationEventExecutionPlan.getAuthenticationHandlers()
                    .stream()
                    .filter(handler -> successfulHandlerNames.contains(handler.getName()))
                    .collect(Collectors.toSet());
                val criteria = authenticationPolicy.getCriteria();
                if (criteria != null) {
                    val policy = criteria.toAuthenticationPolicy(registeredService);
                    val result = policy.isSatisfiedBy(authentication, assertedHandlers, applicationContext, Optional.empty());
                    return result.isSuccess();
                }
            }
        } finally {
            AuthenticationCredentialsThreadLocalBinder.bindCurrent(ca);
        }
        return true;
    }

    @Override
    public boolean supports(final SingleSignOnParticipationRequest ssoRequest) {
        val registeredService = getRegisteredService(ssoRequest);
        if (registeredService == null) {
            return false;
        }
        val authenticationPolicy = registeredService.getAuthenticationPolicy();
        return authenticationPolicy != null && authenticationPolicy.getCriteria() != null;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
