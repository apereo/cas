package org.apereo.cas.adaptors.gauth.web.flow;

import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import javax.annotation.Resource;

/**
 * This is {@link GoogleAuthenticatorAuthenticationWebflowAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class GoogleAuthenticatorAuthenticationWebflowAction extends AbstractAction {
    @Autowired
    @Qualifier("googleAuthenticatorAuthenticationWebflowEventResolver")
    private CasWebflowEventResolver casWebflowEventResolver;

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        return this.casWebflowEventResolver.resolveSingle(requestContext);
    }
}
