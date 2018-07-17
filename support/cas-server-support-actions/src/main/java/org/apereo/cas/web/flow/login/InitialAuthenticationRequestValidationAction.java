package org.apereo.cas.web.flow.login;

import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
public class InitialAuthenticationRequestValidationAction extends AbstractAction {

    private final CasWebflowEventResolver rankedAuthenticationProviderWebflowEventResolver;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        return this.rankedAuthenticationProviderWebflowEventResolver.resolveSingle(requestContext);
    }
}
