package org.apereo.cas.web.flow.actions;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.engine.FlowVariable;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link ClearWebflowCredentialAction} invoked ONLY as an exit-action for non-interactive authn flows.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

@Slf4j
public class ClearWebflowCredentialAction extends AbstractAction {


    @Override
    @SneakyThrows
    protected Event doExecute(final RequestContext requestContext) {
        WebUtils.putCredential(requestContext, null);

        final String current = requestContext.getCurrentEvent().getId();
        if (current.equalsIgnoreCase(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE)
            || current.equalsIgnoreCase(CasWebflowConstants.TRANSITION_ID_ERROR)) {
            LOGGER.debug("Current event signaled a failure. Recreating credentials instance from the context");

            final Flow flow = (Flow) requestContext.getFlowExecutionContext().getDefinition();
            final FlowVariable var = flow.getVariable(CasWebflowConstants.VAR_ID_CREDENTIAL);
            var.create(requestContext);

        }
        return null;
    }
}
