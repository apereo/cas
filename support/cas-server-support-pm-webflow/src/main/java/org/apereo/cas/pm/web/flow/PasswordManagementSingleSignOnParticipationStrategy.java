package org.apereo.cas.pm.web.flow;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.authentication.AuthenticationServiceSelectionPlan;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.registry.TicketRegistrySupport;
import org.apereo.cas.web.flow.BaseSingleSignOnParticipationStrategy;
import org.apereo.cas.web.flow.SingleSignOnParticipationRequest;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link PasswordManagementSingleSignOnParticipationStrategy}.
 *
 * @author Julien Huon
 * @since 6.3.0
 */
@Slf4j
public class PasswordManagementSingleSignOnParticipationStrategy extends BaseSingleSignOnParticipationStrategy {
    private final CentralAuthenticationService centralAuthenticationService;

    public PasswordManagementSingleSignOnParticipationStrategy(final ServicesManager servicesManager,
                                                               final TicketRegistrySupport ticketRegistrySupport,
                                                               final AuthenticationServiceSelectionPlan serviceSelectionStrategy,
                                                               final CentralAuthenticationService centralAuthenticationService) {
        super(servicesManager, ticketRegistrySupport, serviceSelectionStrategy);
        this.centralAuthenticationService = centralAuthenticationService;
    }

    @Override
    public boolean isParticipating(final SingleSignOnParticipationRequest ssoRequest) {
        val token = ssoRequest.getRequestParameter(PasswordManagementWebflowUtils.REQUEST_PARAMETER_NAME_PASSWORD_RESET_TOKEN);
        try {
            if (token.isPresent() && StringUtils.isNotBlank(token.get())) {
                val ticket = centralAuthenticationService.getTicket(token.get(), TransientSessionTicket.class);
                LOGGER.trace("Token ticket [{}] is valid. SSO will be disabled to allow password-resets", ticket);
                return false;
            }
        } catch (final Exception e) {
            LOGGER.trace("Token ticket [{}] is not found or has expired. SSO will not be disabled", token);
        }
        return true;
    }

    @Override
    public boolean supports(final SingleSignOnParticipationRequest ssoRequest) {
        val token = ssoRequest.getRequestParameter(PasswordManagementWebflowUtils.REQUEST_PARAMETER_NAME_PASSWORD_RESET_TOKEN);
        return token.isPresent() && StringUtils.isNotBlank(token.get());
    }
}
