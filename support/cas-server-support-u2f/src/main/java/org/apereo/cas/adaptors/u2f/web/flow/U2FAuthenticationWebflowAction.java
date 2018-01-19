package org.apereo.cas.adaptors.u2f.web.flow;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.web.flow.resolver.CasWebflowEventResolver;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link U2FAuthenticationWebflowAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
@AllArgsConstructor
public class U2FAuthenticationWebflowAction extends AbstractAction {

    private final CasWebflowEventResolver u2fAuthenticationWebflowEventResolver;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        return this.u2fAuthenticationWebflowEventResolver.resolveSingle(requestContext);
    }
}
