package org.apereo.cas.support.inwebo.web.flow.actions;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * A web action to enable the enrollment.
 *
 * @author Jerome LELEU
 * @since 6.4.0
 */
@RequiredArgsConstructor
public class InweboMustEnrollAction extends AbstractAction {

    /**
     * Add error message to context.
     *
     * @param messageContext the message context
     * @param code           the code
     */
    protected static void addErrorMessageToContext(final MessageContext messageContext, final String code) {
        val message = new MessageBuilder().error().code(code).build();
        messageContext.addMessage(message);
    }

    @Override
    public Event doExecute(final RequestContext requestContext) {
        val flowScope = requestContext.getFlowScope();
        flowScope.put(WebflowConstants.MUST_ENROLL, true);
        addErrorMessageToContext(requestContext.getMessageContext(), "cas.inwebo.error.usernotregistered");
        return success();
    }
}
