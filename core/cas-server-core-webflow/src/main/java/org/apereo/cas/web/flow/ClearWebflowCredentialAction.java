package org.apereo.cas.web.flow;

import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link ClearWebflowCredentialAction}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class ClearWebflowCredentialAction extends AbstractAction {

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        WebUtils.putCredential(requestContext, null);
        return null;
    }
}
