package org.apereo.cas.mfa.accepto.web.flow;

import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;

import lombok.RequiredArgsConstructor;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link AccepttoMultifactorFinalizeAuthenticationWebflowAction}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiredArgsConstructor
public class AccepttoMultifactorFinalizeAuthenticationWebflowAction extends AbstractAction {
    private final CasWebflowEventResolver casWebflowEventResolver;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        return this.casWebflowEventResolver.resolveSingle(requestContext);
    }
}
