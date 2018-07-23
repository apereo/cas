package org.apereo.cas.adaptors.authy.web.flow;

import org.apereo.cas.adaptors.authy.AuthyClientInstance;
import org.apereo.cas.web.support.WebUtils;

import com.authy.api.Hash;
import com.authy.api.User;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
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
public class AuthyAuthenticationRegistrationWebflowAction extends AbstractAction {
    private final AuthyClientInstance instance;

    @Override
    protected Event doExecute(final RequestContext context) {
        val principal = WebUtils.getAuthentication(context).getPrincipal();
        val user = instance.getOrCreateUser(principal);
        if (!user.isOk()) {
            throw new IllegalArgumentException(AuthyClientInstance.getErrorMessage(user.getError()));
        }
        val h = submitAuthyRegistrationRequest(user);
        if (!h.isOk() || !h.isSuccess()) {
            throw new IllegalArgumentException(AuthyClientInstance.getErrorMessage(h.getError()).concat(h.getMessage()));
        }
        return success();
    }

    @SneakyThrows
    private Hash submitAuthyRegistrationRequest(final User user) {
        return instance.getAuthyUsers().requestSms(user.getId());
    }
}
