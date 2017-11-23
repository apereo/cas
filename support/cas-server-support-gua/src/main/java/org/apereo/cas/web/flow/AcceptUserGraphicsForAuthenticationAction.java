package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link AcceptUserGraphicsForAuthenticationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class AcceptUserGraphicsForAuthenticationAction extends AbstractAction {

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        final String username = requestContext.getRequestParameters().get("username");
        WebUtils.putCredential(requestContext, new UsernamePasswordCredential(username, null));
        requestContext.getFlowScope().put("guaUsername", username);
        return success();
    }
}
