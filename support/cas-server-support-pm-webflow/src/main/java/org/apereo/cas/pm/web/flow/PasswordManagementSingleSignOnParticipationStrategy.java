package org.apereo.cas.pm.web.flow;

import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;

import lombok.RequiredArgsConstructor;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link PasswordManagementSingleSignOnParticipationStrategy}.
 *
 * @author Julien Huon
 * @since 6.3.0
 */
@RequiredArgsConstructor
public class PasswordManagementSingleSignOnParticipationStrategy implements SingleSignOnParticipationStrategy {
    private final TicketRegistry ticketRegistry;

    @Override
    public boolean supports(final RequestContext requestContext) {
        return PasswordManagementWebflowUtils.isPasswordResetRequestIsValid(requestContext, ticketRegistry);
    }

    @Override
    public boolean isParticipating(final RequestContext requestContext) {
        return false;
    }
}
