package org.apereo.cas.adaptors.authy.web.flow;

import com.authy.api.Hash;
import com.authy.api.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.adaptors.authy.core.AuthyClientInstance;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link AuthyAuthenticationRegistrationWebflowAction}.
 *
 * @author Jérémie POISSON
 * 
 */
@RequiredArgsConstructor
@Slf4j
public class AuthyAuthenticationRegistrationWebflowAction extends BaseCasWebflowAction {
    private static final String MESSAGE_CODE_ERROR = "cas.mfa.authy.error.authn";

    private final AuthyClientInstance instance;

    @Override
    protected Event doExecute(final RequestContext context) throws Exception {

        System.out.println("**************************************");
        System.out.println("AuthyAuthenticationRegistrationWebflowAction");
        System.out.println("**************************************");

        System.out.println("principal: "+ WebUtils.getAuthentication(context).getPrincipal());

        return success();
    }

    /**
     * Add error message.
     *
     * @param requestContext the request context
     * @param exception      the exception
     */
    protected void addErrorMessage(final RequestContext requestContext,
                                   final Exception exception) {
        WebUtils.addErrorMessageToContext(requestContext, MESSAGE_CODE_ERROR, exception.getMessage());
    }

    private Hash submitAuthyRegistrationRequest(final User user) throws Exception {
        return instance.getAuthyClient().getUsers().requestSms(user.getId());
    }
}
