package org.apereo.cas.mfa.accepto.web.flow;

import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;

import lombok.RequiredArgsConstructor;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link AccepttoMultifactorFinalizeAuthenticationWebflowAction}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiredArgsConstructor
public class AccepttoMultifactorFinalizeAuthenticationWebflowAction extends BaseCasWebflowAction {
    private final CasWebflowEventResolver casWebflowEventResolver;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        return this.casWebflowEventResolver.resolveSingle(requestContext);
    }
}
