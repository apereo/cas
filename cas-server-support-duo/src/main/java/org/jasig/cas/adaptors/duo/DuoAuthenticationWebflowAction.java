package org.jasig.cas.adaptors.duo;

import org.jasig.cas.web.flow.authentication.CasWebflowEventResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link DuoAuthenticationWebflowAction}.
 *
 * @author Misagh Moayyed
 * @since 4.3.0
 */
@Component("duoAuthenticationWebflowAction")
public class DuoAuthenticationWebflowAction extends AbstractAction {
    @Autowired
    @Qualifier("duoAuthenticationWebflowEventResolver")
    private CasWebflowEventResolver duoAuthenticationWebflowEventResolver;

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        return duoAuthenticationWebflowEventResolver.resolveSingle(requestContext);
    }
}
