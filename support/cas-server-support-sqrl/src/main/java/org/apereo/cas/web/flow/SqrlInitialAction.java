package org.apereo.cas.web.flow;

import com.github.dbadia.sqrl.server.SqrlConfig;
import com.github.dbadia.sqrl.server.SqrlServerOperations;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link SqrlInitialAction}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class SqrlInitialAction extends AbstractAction {
    private final SqrlConfig sqrlConfig;
    private final SqrlServerOperations sqrlServerOperations;

    public SqrlInitialAction(final SqrlConfig sqrlConfig, final SqrlServerOperations sqrlServerOperations) {
        this.sqrlConfig = sqrlConfig;
        this.sqrlServerOperations = sqrlServerOperations;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        requestContext.getFlowScope().put("sqrlEnabled", Boolean.TRUE);
        return null;
    }
}
