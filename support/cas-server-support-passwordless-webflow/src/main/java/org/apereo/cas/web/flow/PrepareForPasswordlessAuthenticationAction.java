package org.apereo.cas.web.flow;

import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;

import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link PrepareForPasswordlessAuthenticationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
public class PrepareForPasswordlessAuthenticationAction extends BaseCasWebflowAction {

    @Override
    public Event doExecute(final RequestContext requestContext) throws Exception {
        WebUtils.putPasswordlessAuthenticationEnabled(requestContext, Boolean.TRUE);
        if (!WebUtils.hasPasswordlessAuthenticationAccount(requestContext) && isLoginFlowActive(requestContext)) {
            return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_PASSWORDLESS_GET_USERID);
        }
        return null;
    }
}
