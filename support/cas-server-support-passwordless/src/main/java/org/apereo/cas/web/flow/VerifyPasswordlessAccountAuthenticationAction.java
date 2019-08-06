package org.apereo.cas.web.flow;

import org.apereo.cas.api.PasswordlessTokenRepository;
import org.apereo.cas.api.PasswordlessUserAccountStore;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class VerifyPasswordlessAccountAuthenticationAction extends AbstractAction {
    private final PasswordlessTokenRepository passwordlessTokenRepository;
    private final PasswordlessUserAccountStore passwordlessUserAccountStore;

    @Override
    public Event doExecute(final RequestContext requestContext) throws Exception {
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
        return success();
    }
}
