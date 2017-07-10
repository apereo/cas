package org.apereo.cas.web.flow;

import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link SqrlCleanUpAction}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class SqrlCleanUpAction extends AbstractAction {


    public SqrlCleanUpAction() {

    }

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        
        return success();
    }
}
