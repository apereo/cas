package org.apereo.cas.support.inwebo.web.flow.actions;

import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * A simple web action for success.
 *
 * @author Jerome LELEU
 * @since 6.3.0
 */
public class SuccessAction extends AbstractAction {

    @Override
    public Event doExecute(final RequestContext requestContext) {
        return success();
    }
}
