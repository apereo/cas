package org.apereo.cas.web.flow;

import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.annotation.Resource;

/**
 * This is {@link InitialAuthenticationRequestValidationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class InitialAuthenticationRequestValidationAction extends AbstractAction {
    @Autowired
    @Qualifier("rankedAuthenticationProviderWebflowEventResolver")
    private CasWebflowEventResolver rankedAuthenticationProviderWebflowEventResolver;

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        return this.rankedAuthenticationProviderWebflowEventResolver.resolveSingle(requestContext);
    }
}
