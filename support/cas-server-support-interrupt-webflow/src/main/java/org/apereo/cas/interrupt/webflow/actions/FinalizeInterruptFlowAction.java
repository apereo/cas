package org.apereo.cas.interrupt.webflow.actions;

import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.interrupt.InterruptResponse;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link FinalizeInterruptFlowAction}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class FinalizeInterruptFlowAction extends AbstractAction {
    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        final Authentication authentication = WebUtils.getAuthentication(requestContext);
        final RegisteredService registeredService = WebUtils.getRegisteredService(requestContext);
        final InterruptResponse response = requestContext.getFlowScope().get("interrupt", InterruptResponse.class);
        
        return success();
    }
}
