package org.apereo.cas.web.flow;

import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
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
@Slf4j
public class VerifyPasswordlessAccountAuthenticationAction extends AbstractAction {
    private final PasswordlessUserAccountStore passwordlessUserAccountStore;

    /**
     * Add error message to context.
     *
     * @param messageContext the message context
     * @param code           the code
     */
    protected static void addErrorMessageToContext(final MessageContext messageContext, final String code) {
        try {
            val message = new MessageBuilder().error().code(code).build();
            messageContext.addMessage(message);
        } catch (final Exception e) {
            LOGGER.debug(e.getMessage(), e);
        }
    }

    @Override
    public Event doExecute(final RequestContext requestContext) {
        val messageContext = requestContext.getMessageContext();
        val username = requestContext.getRequestParameters().get("username");
        if (StringUtils.isBlank(username)) {
            addErrorMessageToContext(messageContext, "passwordless.error.unknown.user");
            return error();
        }
        val account = passwordlessUserAccountStore.findUser(username);
        if (account.isEmpty()) {
            addErrorMessageToContext(messageContext, "passwordless.error.unknown.user");
            return error();
        }
        val user = account.get();
        if (StringUtils.isBlank(user.getPhone()) && StringUtils.isBlank(user.getEmail())) {
            addErrorMessageToContext(messageContext, "passwordless.error.invalid.user");
            return error();
        }
        WebUtils.putPasswordlessAuthenticationAccount(requestContext, user);
        if (user.isRequestPassword()) {
            return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_PROMPT);
        }
        return success();
    }
}
