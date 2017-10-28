package org.apereo.cas.interrupt.webflow.actions;

import org.apereo.cas.interrupt.InterruptResponse;
import org.apereo.cas.interrupt.webflow.InterruptUtils;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.services.UnauthorizedServiceException;
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
        final RegisteredService registeredService = WebUtils.getRegisteredService(requestContext);
        final InterruptResponse response = InterruptUtils.getInterruptFrom(requestContext);
        
        if (response.isBlock()) {
            if (registeredService != null && registeredService.getAccessStrategy().getUnauthorizedRedirectUrl() != null) {
                final String url = registeredService.getAccessStrategy().getUnauthorizedRedirectUrl().toURL().toExternalForm();
                requestContext.getExternalContext().requestExternalRedirect(url);
                requestContext.getExternalContext().recordResponseComplete();
                return no();
            }
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, "Denied");
        }
        return success();
    }
}
