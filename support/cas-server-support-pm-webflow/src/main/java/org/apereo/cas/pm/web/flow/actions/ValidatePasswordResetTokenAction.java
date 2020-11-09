package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.pm.PasswordManagementService;
import org.apereo.cas.pm.web.flow.PasswordManagementWebflowUtils;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
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
public class ValidatePasswordResetTokenAction extends AbstractAction {
    private final PasswordManagementService passwordManagementService;

    private final CentralAuthenticationService centralAuthenticationService;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        try {
            val transientTicket = requestContext.getRequestParameters()
                .get(PasswordManagementWebflowUtils.REQUEST_PARAMETER_NAME_PASSWORD_RESET_TOKEN);
            if (StringUtils.isNotBlank(transientTicket)) {
                val tst = centralAuthenticationService.getTicket(transientTicket, TransientSessionTicket.class);
                val token = tst.getProperties().get(PasswordManagementWebflowUtils.FLOWSCOPE_PARAMETER_NAME_TOKEN).toString();
                val username = passwordManagementService.parseToken(token);
                if (StringUtils.isBlank(username)) {
                    throw new IllegalArgumentException("Password reset token could not be verified to determine username");
                }
            }
            return null;
        } catch (final Exception e) {
            LoggingUtils.warn(LOGGER, e);
            return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_INVALID_PASSWORD_RESET_TOKEN);
        }
    }
}
