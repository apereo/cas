package org.apereo.cas.adaptors.authy.web.flow;

import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link AuthyAuthenticationWebflowAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class AuthyAuthenticationWebflowAction extends AbstractAction {
    private CasWebflowEventResolver casWebflowEventResolver;

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        return this.casWebflowEventResolver.resolveSingle(requestContext);
    }

    public void setCasWebflowEventResolver(final CasWebflowEventResolver casWebflowEventResolver) {
        this.casWebflowEventResolver = casWebflowEventResolver;
    }
}
