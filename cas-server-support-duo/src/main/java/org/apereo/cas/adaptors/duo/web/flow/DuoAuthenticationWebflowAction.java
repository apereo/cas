package org.apereo.cas.adaptors.duo.web.flow;

import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link DuoAuthenticationWebflowAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class DuoAuthenticationWebflowAction extends AbstractAction {
    private CasWebflowEventResolver duoAuthenticationWebflowEventResolver;

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        return this.duoAuthenticationWebflowEventResolver.resolveSingle(requestContext);
    }

    public void setDuoAuthenticationWebflowEventResolver(final CasWebflowEventResolver duoAuthenticationWebflowEventResolver) {
        this.duoAuthenticationWebflowEventResolver = duoAuthenticationWebflowEventResolver;
    }
}
