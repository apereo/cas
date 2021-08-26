package org.apereo.cas.otp.web.flow;

import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link OneTimeTokenAccountConfirmSelectionRegistrationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiredArgsConstructor
public class OneTimeTokenAccountConfirmSelectionRegistrationAction extends AbstractAction {
    /**
     * Account id parameter.
     */
    public static final String REQUEST_PARAMETER_ACCOUNT_ID = "accountId";

    private final OneTimeTokenCredentialRepository repository;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val id = requestContext.getRequestParameters().getRequired(REQUEST_PARAMETER_ACCOUNT_ID, Long.class);
        WebUtils.putOneTimeTokenAccount(requestContext, repository.get(id));
        return success();
    }
}
