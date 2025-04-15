package org.apereo.cas.web.flow;

import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;

import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link PrepareForGraphicalAuthenticationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class PrepareForGraphicalAuthenticationAction extends BaseCasWebflowAction {
    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) {
        WebUtils.putGraphicalUserAuthenticationEnabled(requestContext, Boolean.TRUE);
        if (!WebUtils.containsGraphicalUserAuthenticationUsername(requestContext)) {
            return eventFactory.event(this, CasWebflowConstants.TRANSITION_ID_GUA_GET_USERID);
        }
        return null;
    }
}
