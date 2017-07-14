package org.apereo.cas.pm.web.flow.actions.acct;

import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link GroovyAccountStateCheckAction}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class GroovyAccountStateCheckAction extends BaseAccountStateCheckAction {
    private final String script;

    public GroovyAccountStateCheckAction(final String script) {
        this.script = script;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        return super.doExecute(requestContext);
    }
}
