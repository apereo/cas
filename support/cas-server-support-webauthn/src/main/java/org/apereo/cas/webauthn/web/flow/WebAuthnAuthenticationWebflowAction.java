package org.apereo.cas.webauthn.web.flow;

import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;

import lombok.RequiredArgsConstructor;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link WebAuthnAuthenticationWebflowAction}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@RequiredArgsConstructor
public class WebAuthnAuthenticationWebflowAction extends AbstractAction {

    private final CasWebflowEventResolver authenticationWebflowEventResolver;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        return this.authenticationWebflowEventResolver.resolveSingle(requestContext);
    }
}
