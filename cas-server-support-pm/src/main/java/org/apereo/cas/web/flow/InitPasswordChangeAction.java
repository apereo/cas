package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.model.support.pm.PasswordManagementProperties;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link InitPasswordChangeAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class InitPasswordChangeAction extends AbstractAction {
    private PasswordManagementProperties properties;

    public InitPasswordChangeAction(final PasswordManagementProperties properties) {
        this.properties = properties;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        requestContext.getFlowScope().put("passwordManagementEnabled", properties.isEnabled());
        return null;
    }
}
