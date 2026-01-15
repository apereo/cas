package org.apereo.cas.web.flow;

import module java.base;
import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.support.WebUtils;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link AcceptUserGraphicsForAuthenticationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class AcceptUserGraphicsForAuthenticationAction extends BaseCasWebflowAction {

    @Override
    protected @Nullable Event doExecuteInternal(final RequestContext requestContext) {
        val username = requestContext.getRequestParameters().get("username");
        WebUtils.putCredential(requestContext, new UsernamePasswordCredential(username, null));
        WebUtils.putGraphicalUserAuthenticationUsername(requestContext, username);
        return success();
    }
}
