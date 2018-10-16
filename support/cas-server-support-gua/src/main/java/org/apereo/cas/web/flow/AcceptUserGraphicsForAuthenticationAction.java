package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
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
    public Event doExecute(final RequestContext requestContext) {
        val username = requestContext.getRequestParameters().get("username");
        WebUtils.putCredential(requestContext, new UsernamePasswordCredential(username, null));
        WebUtils.putGraphicalUserAuthenticationUsername(requestContext, username);
        return success();
    }
}
