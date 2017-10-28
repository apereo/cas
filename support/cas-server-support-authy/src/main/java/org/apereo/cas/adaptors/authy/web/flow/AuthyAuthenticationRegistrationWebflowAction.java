package org.apereo.cas.adaptors.authy.web.flow;

import com.authy.api.Hash;
import com.authy.api.User;
import org.apereo.cas.adaptors.authy.AuthyClientInstance;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link AuthyAuthenticationRegistrationWebflowAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class AuthyAuthenticationRegistrationWebflowAction extends AbstractAction {

    private final AuthyClientInstance instance;

    public AuthyAuthenticationRegistrationWebflowAction(final AuthyClientInstance instance) {
        this.instance = instance;
    }

    @Override
    protected Event doExecute(final RequestContext context) {
        final Principal principal = WebUtils.getAuthentication(context).getPrincipal();
        final User user = instance.getOrCreateUser(principal);
        if (!user.isOk()) {
            throw new IllegalArgumentException(AuthyClientInstance.getErrorMessage(user.getError()));
        }
        final Hash h = instance.getAuthyUsers().requestSms(user.getId());
        if (!h.isOk() || !h.isSuccess()) {
            throw new IllegalArgumentException(AuthyClientInstance.getErrorMessage(h.getError()).concat(h.getMessage()));
        }
        return success();
    }
}
