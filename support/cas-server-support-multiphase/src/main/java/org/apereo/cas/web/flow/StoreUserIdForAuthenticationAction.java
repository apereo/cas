package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link StoreUserIdForAuthenticationAction}.
 * @author Hayden Sartoris
 * @since 6.2.0
 */
@RequiredArgsConstructor
public class StoreUserIdForAuthenticationAction extends AbstractAction {
    //private final MultiphaseUserEventResolver multiphaseUserEventResolver;

	@Override
	public Event doExecute(final RequestContext requestContext) {
		val username = requestContext.getRequestParameters().get("username");
        if (username.endsWith("@example.com")) {
            return result(MultiphaseAuthenticationWebflowConfigurer.TRANSITION_ID_MULTIPHASE_REDIRECT);
        }
		WebUtils.putCredential(requestContext, new UsernamePasswordCredential(username, null));
		WebUtils.putMultiphaseAuthenticationUsername(requestContext, username);
		return success();
	}
}
