package org.apereo.cas.interrupt.webflow.actions;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.interrupt.webflow.InterruptUtils;
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
@Slf4j
public class FinalizeInterruptFlowAction extends AbstractAction {
    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        final var registeredService = WebUtils.getRegisteredService(requestContext);
        final var response = InterruptUtils.getInterruptFrom(requestContext);
        
        if (response.isBlock()) {
            final var accessUrl = registeredService.getAccessStrategy().getUnauthorizedRedirectUrl();
            if (registeredService != null && accessUrl != null) {
                final var url = accessUrl.toURL().toExternalForm();
                final var externalContext = requestContext.getExternalContext();
                externalContext.requestExternalRedirect(url);
                externalContext.recordResponseComplete();
                return no();
            }
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, "Denied");
        }
        return success();
    }
}
