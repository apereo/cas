package org.apereo.cas.web.flow.actions;

import org.apereo.cas.web.flow.CasWebflowConfigurer;

import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link BaseCasWebflowAction}.
 *
 * @author Misagh Moayyed
 * @since 6.5.0
 */
public abstract class BaseCasWebflowAction extends AbstractAction {
    /**
     * Is login flow active.
     *
     * @param requestContext the request context
     * @return the boolean
     */
    protected static boolean isLoginFlowActive(final RequestContext requestContext) {
        return requestContext.getActiveFlow().getId().equalsIgnoreCase(CasWebflowConfigurer.FLOW_ID_LOGIN);
    }
}
