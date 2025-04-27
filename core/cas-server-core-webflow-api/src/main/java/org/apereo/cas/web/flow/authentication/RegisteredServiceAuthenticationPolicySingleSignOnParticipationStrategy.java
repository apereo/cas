package org.apereo.cas.web.flow.authentication;

import org.apereo.cas.authentication.AuthenticationEventExecutionPlan;
import org.apereo.cas.authentication.AuthenticationHandler;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.AuthenticationAwareTicket;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.web.flow.BaseSingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.SingleSignOnParticipationRequest;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jooq.lambda.Unchecked;
import org.springframework.context.ConfigurableApplicationContext;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This is {@link RegisteredServiceAuthenticationPolicySingleSignOnParticipationStrategy}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Slf4j
public class RegisteredServiceAuthenticationPolicySingleSignOnParticipationStrategy extends BaseSingleSignOnParticipationStrategy {

    private final AuthenticationEventExecutionPlan authenticationEventExecutionPlan;

    private final ConfigurableApplicationContext applicationContext;

    public RegisteredServiceAuthenticationPolicySingleSignOnParticipationStrategy(
        final ServicesManager servicesManager,
        final TicketRegistrySupport ticketRegistrySupport,
        final AuthenticationServiceSelectionPlan serviceSelectionStrategy,
        final AuthenticationEventExecutionPlan executionPlan,
        final ConfigurableApplicationContext applicationContext) {
        super(servicesManager, ticketRegistrySupport, serviceSelectionStrategy);
        this.authenticationEventExecutionPlan = executionPlan;
        this.applicationContext = applicationContext;
    }

    @Override
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

        val authentication = getTicketState(ssoRequest)
            .map(AuthenticationAwareTicket.class::cast)
            .map(AuthenticationAwareTicket::getAuthentication).orElseThrow();
        val successfulHandlerNames = CollectionUtils.toCollection(authentication.getAttributes()
            .get(AuthenticationHandler.SUCCESSFUL_AUTHENTICATION_HANDLERS));
        val assertedHandlers = authenticationEventExecutionPlan.resolveAuthenticationHandlers()
            .stream()
            .filter(handler -> successfulHandlerNames.contains(handler.getName()))
            .collect(Collectors.toSet());
        LOGGER.debug("Asserted authentication handlers are [{}]", assertedHandlers);
        val criteria = authenticationPolicy.getCriteria();
        return Optional.ofNullable(criteria)
            .map(Unchecked.function(cri -> {
                val policy = criteria.toAuthenticationPolicy(registeredService);
                val policyContext = Map.of(RegisteredService.class.getName(), registeredService);
                val result = policy.isSatisfiedBy(authentication, assertedHandlers, applicationContext, policyContext);
                return result.isSuccess();
            })).orElse(true);
    }

    @Override
    public boolean supports(final SingleSignOnParticipationRequest ssoRequest) {
        val registeredService = getRegisteredService(ssoRequest);
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
}
