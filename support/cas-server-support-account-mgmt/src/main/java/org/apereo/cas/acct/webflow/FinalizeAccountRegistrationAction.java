package org.apereo.cas.acct.webflow;

import org.apereo.cas.acct.AccountRegistrationService;
import org.apereo.cas.acct.AccountRegistrationUtils;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Objects;

/**
 * This is {@link FinalizeAccountRegistrationAction}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
@RequiredArgsConstructor
@Slf4j
public class FinalizeAccountRegistrationAction extends AbstractAction {
    private final AccountRegistrationService accountRegistrationService;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        try {
            val registrationRequest = AccountRegistrationUtils.getAccountRegistrationRequest(requestContext);
            Objects.requireNonNull(registrationRequest).putProperties(requestContext.getRequestParameters().asAttributeMap().asMap());
            val response = accountRegistrationService.getAccountRegistrationProvisioner().provision(registrationRequest);
            if (response.isSuccess()) {
                return success(response);
            }
        } catch (final Exception e) {
            LoggingUtils.error(LOGGER, e);
        }
        WebUtils.addErrorMessageToContext(requestContext, "cas.screen.acct.error.provision");
        return error();
    }
}
