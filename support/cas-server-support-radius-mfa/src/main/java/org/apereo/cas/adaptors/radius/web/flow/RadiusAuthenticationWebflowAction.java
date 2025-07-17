package org.apereo.cas.adaptors.radius.web.flow;

import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;

import lombok.RequiredArgsConstructor;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link RadiusAuthenticationWebflowAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RequiredArgsConstructor
public class RadiusAuthenticationWebflowAction extends BaseCasWebflowAction {
    private final CasWebflowEventResolver radiusAuthenticationWebflowEventResolver;

    @Override
    protected Event doExecuteInternal(final RequestContext requestContext) throws Throwable {
        return radiusAuthenticationWebflowEventResolver.resolveSingle(requestContext);
    }
}

