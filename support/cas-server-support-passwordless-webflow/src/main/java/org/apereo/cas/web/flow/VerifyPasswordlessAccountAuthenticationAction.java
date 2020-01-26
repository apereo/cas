package org.apereo.cas.web.flow;

import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.webflow.action.AbstractAction;
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
        val messageContext = requestContext.getMessageContext();
        val username = requestContext.getRequestParameters().get("username");
        if (StringUtils.isBlank(username)) {
            val message = new MessageBuilder().error().code("passwordless.error.unknown.user").build();
            messageContext.addMessage(message);
            return error();
        }
        val account = passwordlessUserAccountStore.findUser(username);
        if (account.isEmpty()) {
            val message = new MessageBuilder().error().code("passwordless.error.unknown.user").build();
            messageContext.addMessage(message);
            return error();
        }
        val user = account.get();
        if (StringUtils.isBlank(user.getPhone()) && StringUtils.isBlank(user.getEmail())) {
            val message = new MessageBuilder().error().code("passwordless.error.invalid.user").build();
            messageContext.addMessage(message);
            return error();
        }
        WebUtils.putPasswordlessAuthenticationAccount(requestContext, user);
        return success();
    }
}
