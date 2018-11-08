package org.apereo.cas.web.flow.login;

import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;

import lombok.RequiredArgsConstructor;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link InitialAuthenticationRequestValidationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiredArgsConstructor
public class InitialAuthenticationRequestValidationAction extends AbstractAction {

    private final CasWebflowEventResolver initialAuthenticationProviderWebflowEventResolver;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        return this.initialAuthenticationProviderWebflowEventResolver.resolveSingle(requestContext);
    }
}
