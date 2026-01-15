package org.apereo.cas.interrupt.webflow;

import module java.base;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.configuration.support.TriStateBoolean;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.flow.BaseSingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.SingleSignOnParticipationRequest;

/**
 * This is {@link InterruptSingleSignOnParticipationStrategy}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class InterruptSingleSignOnParticipationStrategy extends BaseSingleSignOnParticipationStrategy {

    public InterruptSingleSignOnParticipationStrategy(final ServicesManager servicesManager,
                                                      final TicketRegistrySupport ticketRegistrySupport,
                                                      final AuthenticationServiceSelectionPlan serviceSelectionStrategy) {
        super(servicesManager, ticketRegistrySupport, serviceSelectionStrategy);
    }

    @Override
    public boolean supports(final SingleSignOnParticipationRequest ssoRequest) {
        return ssoRequest.getRequestContext()
            .stream()
            .map(InterruptUtils::getInterruptFrom)
            .anyMatch(Objects::nonNull);
    }

    @Override
    public boolean isParticipating(final SingleSignOnParticipationRequest ssoRequest) {
        return ssoRequest.getRequestContext()
            .stream()
            .map(InterruptUtils::getInterruptFrom)
            .allMatch(response -> response != null && response.isSsoEnabled());
    }

    @Override
    public TriStateBoolean isCreateCookieOnRenewedAuthentication(final SingleSignOnParticipationRequest context) {
        return TriStateBoolean.FALSE;
    }
}
