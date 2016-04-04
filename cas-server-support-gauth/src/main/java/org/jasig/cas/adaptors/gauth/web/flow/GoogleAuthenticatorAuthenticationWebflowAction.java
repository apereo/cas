package org.jasig.cas.adaptors.gauth.web.flow;

import org.jasig.cas.web.flow.resolver.CasWebflowEventResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link GoogleAuthenticatorAuthenticationWebflowAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RefreshScope
@Component("googleAuthenticatorAuthenticationWebflowAction")
public class GoogleAuthenticatorAuthenticationWebflowAction extends AbstractAction {
    @Autowired
    @Qualifier("googleAuthenticatorAuthenticationWebflowEventResolver")
    private CasWebflowEventResolver casWebflowEventResolver;

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        return this.casWebflowEventResolver.resolveSingle(requestContext);
    }
}
