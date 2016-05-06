package org.apereo.cas.adaptors.duo.web.flow;

import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link DuoAuthenticationWebflowAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@RefreshScope
@Component("duoAuthenticationWebflowAction")
public class DuoAuthenticationWebflowAction extends AbstractAction {
    @Autowired
    @Qualifier("duoAuthenticationWebflowEventResolver")
    private CasWebflowEventResolver duoAuthenticationWebflowEventResolver;

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        return this.duoAuthenticationWebflowEventResolver.resolveSingle(requestContext);
    }
}
