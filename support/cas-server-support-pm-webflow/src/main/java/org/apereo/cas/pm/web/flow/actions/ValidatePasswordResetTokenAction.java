package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link ValidatePasswordResetTokenAction}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiredArgsConstructor
@Slf4j
public class ValidatePasswordResetTokenAction extends BaseCasWebflowAction {
    private final PasswordManagementService passwordManagementService;

    private final TicketRegistry ticketRegistry;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        try {
            val transientTicket = requestContext.getRequestParameters()
                .get(PasswordManagementService.PARAMETER_PASSWORD_RESET_TOKEN);
            if (StringUtils.isNotBlank(transientTicket)) {
                val tst = ticketRegistry.getTicket(transientTicket, TransientSessionTicket.class);
                val token = tst.getProperties().get(PasswordManagementService.PARAMETER_TOKEN).toString();
                val username = passwordManagementService.parseToken(token);
                if (StringUtils.isBlank(username)) {
                    throw new IllegalArgumentException("Password reset token could not be verified to determine username");
                }
                return eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_RESET_PASSWORD);
            }
            val doChange = requestContext.getRequestParameters()
                .get(PasswordManagementService.PARAMETER_DO_CHANGE_PASSWORD);
            if (StringUtils.isNotBlank(doChange) && BooleanUtils.toBoolean(doChange)) {
                return eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_RESET_PASSWORD);
            }

            return null;
        } catch (final Exception e) {
            LoggingUtils.warn(LOGGER, e);
            return eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_INVALID_PASSWORD_RESET_TOKEN);
        }
    }
}
