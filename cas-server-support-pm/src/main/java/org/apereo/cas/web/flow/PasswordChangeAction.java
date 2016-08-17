package org.apereo.cas.web.flow;

import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link PasswordChangeAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class PasswordChangeAction extends AbstractAction {
    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        requestContext.getFlowScope().put("passwordManagementEnabled", Boolean.TRUE);
        return null;
    }
}
