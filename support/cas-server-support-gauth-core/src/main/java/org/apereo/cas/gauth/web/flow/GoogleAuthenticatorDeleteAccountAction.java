package org.apereo.cas.gauth.web.flow;

import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.otp.web.flow.OneTimeTokenAccountConfirmSelectionRegistrationAction;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link GoogleAuthenticatorDeleteAccountAction}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiredArgsConstructor
public class GoogleAuthenticatorDeleteAccountAction extends BaseCasWebflowAction {
    private final OneTimeTokenCredentialRepository repository;

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        val id = requestContext.getRequestParameters()
            .getRequired(OneTimeTokenAccountConfirmSelectionRegistrationAction.REQUEST_PARAMETER_ACCOUNT_ID, Long.class);
        repository.delete(id);
        return success();
    }
}
