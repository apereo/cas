package org.apereo.cas.pm.web.flow;

import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
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
        val transientTicket = requestContext
            .getRequestParameters()
            .get(PasswordManagementWebflowUtils.REQUEST_PARAMETER_NAME_PASSWORD_RESET_TOKEN);

        if (StringUtils.isBlank(transientTicket)) {
            return false;
        }

        return ticketRegistry.getTicket(transientTicket) != null;
    }

    @Override
    public boolean isParticipating(final RequestContext requestContext) {
        return false;
    }
}
