package org.apereo.cas.adaptors.authy.web.flow;

import org.apereo.cas.adaptors.authy.AuthyClientInstance;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.support.WebUtils;

import com.authy.api.Hash;
import com.authy.api.User;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link AuthyAuthenticationRegistrationWebflowAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class AuthyAuthenticationRegistrationWebflowAction extends AbstractAction {
    private static final String MESSAGE_CODE_ERROR = "cas.mfa.authy.error.authn";

    private final AuthyClientInstance instance;

    @Override
    protected Event doExecute(final RequestContext context) {
        val principal = WebUtils.getAuthentication(context).getPrincipal();
        val user = instance.getOrCreateUser(principal);
        if (!user.isOk()) {
            val ex = new IllegalArgumentException(AuthyClientInstance.getErrorMessage(user.getError()));
            addErrorMessage(context, ex);
            LoggingUtils.error(LOGGER, ex);
            return error(ex);
        }
        val h = submitAuthyRegistrationRequest(user);
        if (!h.isOk() || !h.isSuccess()) {
            val ex = new IllegalArgumentException(AuthyClientInstance.getErrorMessage(h.getError()).concat(h.getMessage()));
            LoggingUtils.error(LOGGER, ex);
            return error(ex);
        }
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

    @SneakyThrows
    private Hash submitAuthyRegistrationRequest(final User user) {
        return instance.getAuthyUsers().requestSms(user.getId());
    }
}
