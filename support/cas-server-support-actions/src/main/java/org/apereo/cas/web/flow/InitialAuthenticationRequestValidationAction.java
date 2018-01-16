package org.apereo.cas.web.flow;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link InitialAuthenticationRequestValidationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
public class InitialAuthenticationRequestValidationAction extends AbstractAction {

    private final CasWebflowEventResolver rankedAuthenticationProviderWebflowEventResolver;

    public InitialAuthenticationRequestValidationAction(final CasWebflowEventResolver eventResolver) {
        this.rankedAuthenticationProviderWebflowEventResolver = eventResolver;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        return this.rankedAuthenticationProviderWebflowEventResolver.resolveSingle(requestContext);
    }
}
