package org.apereo.cas.pm.web.flow;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.web.flow.SingleSignOnParticipationStrategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link PasswordManagementSingleSignOnParticipationStrategy}.
 *
 * @author Julien Huon
 * @since 6.3.0
 */
@Slf4j
@RequiredArgsConstructor
public class PasswordManagementSingleSignOnParticipationStrategy implements SingleSignOnParticipationStrategy {
    private final CentralAuthenticationService centralAuthenticationService;

    @Override
    public boolean isParticipating(final RequestContext requestContext) {
        val transientTicket = requestContext
            .getRequestParameters()
            .get(PasswordManagementWebflowUtils.REQUEST_PARAMETER_NAME_PASSWORD_RESET_TOKEN);
        try {
            val ticket = centralAuthenticationService.getTicket(transientTicket, TransientSessionTicket.class);
            LOGGER.trace("Token ticket [{}] is valid. SSO will be disabled to allow password-resets", ticket);
            return false;
        } catch (final Exception e) {
            LOGGER.trace("Token ticket [{}] is not found or has expired. SSO will not be disabled", transientTicket);
        }
        return true;
    }

    @Override
    public boolean supports(final RequestContext requestContext) {
        val transientTicket = requestContext
            .getRequestParameters()
            .get(PasswordManagementWebflowUtils.REQUEST_PARAMETER_NAME_PASSWORD_RESET_TOKEN);
        return StringUtils.isNotBlank(transientTicket);
    }
}
