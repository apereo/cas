package org.apereo.cas.interrupt.webflow.actions;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.interrupt.InterruptInquirer;
import org.apereo.cas.interrupt.InterruptResponse;
import org.apereo.cas.interrupt.webflow.InterruptUtils;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link InquireInterruptAction}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class InquireInterruptAction extends AbstractAction {
    private final InterruptInquirer interruptInquirer;

    public InquireInterruptAction(final InterruptInquirer interruptInquirer) {
        this.interruptInquirer = interruptInquirer;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        final Authentication authentication = WebUtils.getAuthentication(requestContext);
        final Service service = WebUtils.getService(requestContext);
        final RegisteredService registeredService = WebUtils.getRegisteredService(requestContext);
        
        final InterruptResponse response = this.interruptInquirer.inquire(authentication, registeredService, service);
        if (response == null || !response.isInterrupt()) {
            return no();
        }
        InterruptUtils.putInterruptIn(requestContext, response);
        WebUtils.putPrincipal(requestContext, authentication.getPrincipal());
        return yes();
    }
}
