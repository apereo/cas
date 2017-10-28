package org.apereo.cas.otp.web.flow;

import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link org.apereo.cas.otp.web.flow.OneTimeTokenAuthenticationWebflowAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OneTimeTokenAuthenticationWebflowAction extends AbstractAction {

    private final CasWebflowEventResolver casWebflowEventResolver;

    public OneTimeTokenAuthenticationWebflowAction(final CasWebflowEventResolver casWebflowEventResolver) {
        this.casWebflowEventResolver = casWebflowEventResolver;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        return this.casWebflowEventResolver.resolveSingle(requestContext);
    }
}
