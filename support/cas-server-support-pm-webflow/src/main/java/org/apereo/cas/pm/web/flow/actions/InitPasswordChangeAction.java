package org.apereo.cas.pm.web.flow.actions;

import org.apereo.cas.configuration.CasConfigurationProperties;

import lombok.RequiredArgsConstructor;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link InitPasswordChangeAction}, serves a as placeholder for extensions.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiredArgsConstructor
public class InitPasswordChangeAction extends AbstractAction {
    private final CasConfigurationProperties casProperties;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        requestContext.getFlowScope().put("policyPattern", casProperties.getAuthn().getPm().getPolicyPattern());
        return null;
    }
}
