package org.apereo.cas.interrupt.webflow.actions;

import org.apereo.cas.interrupt.InterruptInquirer;
import org.apereo.cas.interrupt.webflow.InterruptUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link InquireInterruptAction}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiredArgsConstructor
public class InquireInterruptAction extends AbstractAction {
    private final InterruptInquirer interruptInquirer;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val authentication = WebUtils.getAuthentication(requestContext);
        val service = WebUtils.getService(requestContext);
        val registeredService = WebUtils.getRegisteredService(requestContext);
        val credential = WebUtils.getCredential(requestContext);

        val response = this.interruptInquirer.inquire(authentication, registeredService, service, credential);
        if (response == null || !response.isInterrupt()) {
            return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_INTERRUPT_SKIPPED);
        }
        InterruptUtils.putInterruptIn(requestContext, response);
        WebUtils.putPrincipal(requestContext, authentication.getPrincipal());
        return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_INTERRUPT_REQUIRED);
    }
}
