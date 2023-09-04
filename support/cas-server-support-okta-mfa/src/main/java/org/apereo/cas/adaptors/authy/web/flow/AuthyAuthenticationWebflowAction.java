package org.apereo.cas.adaptors.authy.web.flow;

import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;

import lombok.RequiredArgsConstructor;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link AuthyAuthenticationWebflowAction}.
 *
 * @author Jérémie POISSON
 * 
 */
@RequiredArgsConstructor
public class AuthyAuthenticationWebflowAction extends BaseCasWebflowAction {

    private final CasWebflowEventResolver casWebflowEventResolver;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        System.out.println("**************************************");
        System.out.println("AuthyAuthenticationWebflowAction");
        System.out.println("**************************************");
        return this.casWebflowEventResolver.resolveSingle(requestContext);
    }
}
