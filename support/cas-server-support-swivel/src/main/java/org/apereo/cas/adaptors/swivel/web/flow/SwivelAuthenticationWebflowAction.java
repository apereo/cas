package org.apereo.cas.adaptors.swivel.web.flow;

import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link SwivelAuthenticationWebflowAction}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class SwivelAuthenticationWebflowAction extends AbstractAction {

    private final CasWebflowEventResolver casWebflowEventResolver;

    public SwivelAuthenticationWebflowAction(final CasWebflowEventResolver casWebflowEventResolver) {
        this.casWebflowEventResolver = casWebflowEventResolver;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        return this.casWebflowEventResolver.resolveSingle(requestContext);
    }
}
