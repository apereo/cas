package org.apereo.cas.web.flow.actions;

import org.apereo.cas.web.flow.CasWebflowConfigurer;

import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.ActionExecutionException;
import org.springframework.webflow.execution.Event;
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
     * @return true/false
     */
    protected static boolean isLoginFlowActive(final RequestContext requestContext) {
        return requestContext.getActiveFlow().getId().equalsIgnoreCase(CasWebflowConfigurer.FLOW_ID_LOGIN);
    }

    @Override
    protected final Event doExecute(final RequestContext requestContext) throws Exception {
        try {
            return doExecuteInternal(requestContext);
        } catch (final Exception e) {
            throw e;
        } catch (final Throwable e) {
            throw new ActionExecutionException(requestContext.getActiveFlow().getId(),
                requestContext.getCurrentState().getId(), this,
                requestContext.getAttributes(), e);
        }
    }

    protected abstract Event doExecuteInternal(RequestContext requestContext) throws Throwable;
}
