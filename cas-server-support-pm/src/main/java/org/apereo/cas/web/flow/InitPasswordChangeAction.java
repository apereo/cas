package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private CasConfigurationProperties casProperties;

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        requestContext.getFlowScope().put("passwordManagementEnabled", casProperties.getAuthn().getPm().isEnabled());
        return null;
    }
}
