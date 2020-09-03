package org.apereo.cas.webauthn.web.flow;

import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link WebAuthnAuthenticationWebflowAction}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@RequiredArgsConstructor
public class WebAuthnAuthenticationWebflowAction extends AbstractAction {

    private final CasWebflowEventResolver authenticationWebflowEventResolver;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val result = this.authenticationWebflowEventResolver.resolveSingle(requestContext);
        if (!result.getId().equals(CasWebflowConstants.STATE_ID_SUCCESS)) {
            val messageContext = requestContext.getMessageContext();
            val message = new MessageBuilder()
                .error()
                .code("cas.mfa.webauthn.auth.fail")
                .build();
            messageContext.addMessage(message);
        }
        return result;
    }
}
