package org.apereo.cas.web.flow;

import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link VerifyPasswordlessAccountAuthenticationAction}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiredArgsConstructor
public class VerifyPasswordlessAccountAuthenticationAction extends AbstractAction {
    private final PasswordlessUserAccountStore passwordlessUserAccountStore;

    @Override
    public Event doExecute(final RequestContext requestContext) {
        val username = requestContext.getRequestParameters().getRequired("username");
        val account = passwordlessUserAccountStore.findUser(username);
        if (account.isEmpty()) {
            WebUtils.addErrorMessageToContext(requestContext, "passwordless.error.unknown.user");
            return error();
        }
        val user = account.get();
        if (StringUtils.isBlank(user.getPhone()) && StringUtils.isBlank(user.getEmail())) {
            WebUtils.addErrorMessageToContext(requestContext, "passwordless.error.invalid.user");
            return error();
        }
        WebUtils.putPasswordlessAuthenticationAccount(requestContext, user);
        if (user.isRequestPassword()) {
            return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_PROMPT);
        }
        return success();
    }
}
