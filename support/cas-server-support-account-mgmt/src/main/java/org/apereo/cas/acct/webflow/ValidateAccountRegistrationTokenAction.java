package org.apereo.cas.acct.webflow;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.acct.AccountRegistrationService;
import org.apereo.cas.acct.AccountRegistrationUtils;
import org.apereo.cas.authentication.RootCasException;
import org.apereo.cas.ticket.TicketState;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link ValidateAccountRegistrationTokenAction}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@Slf4j
@RequiredArgsConstructor
public class ValidateAccountRegistrationTokenAction extends AbstractAction {
    private final CentralAuthenticationService centralAuthenticationService;

    private final AccountRegistrationService accountRegistrationService;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        var accountRegTicket = (TransientSessionTicket) null;
        try {
            val activationToken = requestContext.getRequestParameters()
                .getRequired(AccountRegistrationUtils.REQUEST_PARAMETER_ACCOUNT_REGISTRATION_ACTIVATION_TOKEN);
            accountRegTicket = centralAuthenticationService.getTicket(activationToken, TransientSessionTicket.class);
            val token = accountRegTicket.getProperty(AccountRegistrationUtils.PROPERTY_ACCOUNT_REGISTRATION_ACTIVATION_TOKEN, String.class);
            val registrationRequest = accountRegistrationService.validateToken(token);
            ((TicketState) accountRegTicket).update();

            val username = accountRegistrationService.getAccountRegistrationUsernameBuilder().build(registrationRequest);
            AccountRegistrationUtils.putAccountRegistrationRequest(requestContext, registrationRequest);
            AccountRegistrationUtils.putAccountRegistrationRequestUsername(requestContext, username);

            return success(registrationRequest);
        } catch (final Exception e) {
            LoggingUtils.warn(LOGGER, e);
            requestContext.getFlashScope().put(CasWebflowConstants.ATTRIBUTE_ERROR_ROOT_CAUSE_EXCEPTION,
                RootCasException.withCode("screen.error.page.invalidrequest.desc"));
            return error(e);
        } finally {
            if (accountRegTicket != null && accountRegTicket.isExpired()) {
                centralAuthenticationService.deleteTicket(accountRegTicket);
            }
        }
    }
}
