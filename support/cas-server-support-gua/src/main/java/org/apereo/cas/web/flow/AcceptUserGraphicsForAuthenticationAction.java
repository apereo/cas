package org.apereo.cas.web.flow;

import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class AcceptUserGraphicsForAuthenticationAction extends AbstractAction {

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        final String username = requestContext.getRequestParameters().get("username");
        WebUtils.putCredential(requestContext, new UsernamePasswordCredential(username, null));
        requestContext.getFlowScope().put("guaUsername", username);
        return success();
    }
}
