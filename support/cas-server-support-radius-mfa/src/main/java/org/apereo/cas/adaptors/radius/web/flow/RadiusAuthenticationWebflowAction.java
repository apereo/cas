package org.apereo.cas.adaptors.radius.web.flow;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link RadiusAuthenticationWebflowAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */

@Slf4j
public class RadiusAuthenticationWebflowAction extends AbstractAction {
   
    private CasWebflowEventResolver radiusAuthenticationWebflowEventResolver;

    public RadiusAuthenticationWebflowAction(final CasWebflowEventResolver radiusAuthenticationWebflowEventResolver) {
        this.radiusAuthenticationWebflowEventResolver = radiusAuthenticationWebflowEventResolver;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        return this.radiusAuthenticationWebflowEventResolver.resolveSingle(requestContext);
    }
}

