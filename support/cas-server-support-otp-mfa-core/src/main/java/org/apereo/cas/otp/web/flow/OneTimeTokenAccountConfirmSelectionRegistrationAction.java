package org.apereo.cas.otp.web.flow;

import module java.base;
import org.apereo.cas.otp.repository.credentials.OneTimeTokenCredentialRepository;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.flow.util.MultifactorAuthenticationWebflowUtils;
import org.apereo.cas.web.support.WebUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link OneTimeTokenAccountConfirmSelectionRegistrationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiredArgsConstructor
public class OneTimeTokenAccountConfirmSelectionRegistrationAction extends BaseCasWebflowAction {
    /**
     * Account id parameter.
     */
    public static final String REQUEST_PARAMETER_ACCOUNT_ID = "accountId";

    private final OneTimeTokenCredentialRepository repository;

    @Override
    protected @Nullable Event doExecuteInternal(final RequestContext requestContext) {
        val id = Long.parseLong(WebUtils.getRequestParameterOrAttribute(requestContext, REQUEST_PARAMETER_ACCOUNT_ID).orElseThrow());
        MultifactorAuthenticationWebflowUtils.putOneTimeTokenAccount(requestContext, repository.get(id));
        return success();
    }
}
