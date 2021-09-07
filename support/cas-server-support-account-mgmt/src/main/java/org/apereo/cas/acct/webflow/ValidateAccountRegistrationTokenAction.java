package org.apereo.cas.acct.webflow;

import org.apereo.cas.CentralAuthenticationService;
import org.apereo.cas.acct.AccountRegistrationService;
import org.apereo.cas.acct.AccountRegistrationUtils;
import org.apereo.cas.authentication.RootCasException;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

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
        try {
            val transientTicket = requestContext.getRequestParameters()
                .getRequired(AccountRegistrationUtils.REQUEST_PARAMETER_ACCOUNT_REGISTRATION_ACTIVATION_TOKEN);
            val tst = centralAuthenticationService.getTicket(transientTicket, TransientSessionTicket.class);
            val token = tst.getProperty(AccountRegistrationUtils.PROPERTY_ACCOUNT_REGISTRATION_ACTIVATION_TOKEN, String.class);
            val registrationRequest = accountRegistrationService.validateToken(token);
            WebUtils.putAccountManagementRegistrationRequest(requestContext, registrationRequest);
            return success(registrationRequest);
        } catch (final Exception e) {
            LoggingUtils.warn(LOGGER, e);
            requestContext.getFlashScope().put(CasWebflowConstants.ATTRIBUTE_ERROR_ROOT_CAUSE_EXCEPTION,
                RootCasException.withCode("screen.error.page.invalidrequest.desc"));
            return error(e);
        }
    }
}
