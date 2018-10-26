package org.apereo.cas.interrupt.webflow.actions;

import org.apereo.cas.interrupt.webflow.InterruptUtils;
import org.apereo.cas.services.UnauthorizedServiceException;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.action.EventFactorySupport;
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
        val registeredService = WebUtils.getRegisteredService(requestContext);
        val response = InterruptUtils.getInterruptFrom(requestContext);

        if (response.isBlock()) {
            val accessUrl = registeredService != null
                ? registeredService.getAccessStrategy().getUnauthorizedRedirectUrl()
                : null;
            if (accessUrl != null) {
                val url = accessUrl.toURL().toExternalForm();
                val externalContext = requestContext.getExternalContext();
                externalContext.requestExternalRedirect(url);
                externalContext.recordResponseComplete();
                return new EventFactorySupport().event(this, CasWebflowConstants.STATE_ID_STOP_WEBFLOW);
            }
            throw new UnauthorizedServiceException(UnauthorizedServiceException.CODE_UNAUTHZ_SERVICE, "Denied");
        }
        val authentication = WebUtils.getAuthentication(requestContext);
        authentication.addAttribute(InquireInterruptAction.AUTHENTICATION_ATTRIBUTE_FINALIZED_INTERRUPT, Boolean.TRUE);
        return success();
    }
}
