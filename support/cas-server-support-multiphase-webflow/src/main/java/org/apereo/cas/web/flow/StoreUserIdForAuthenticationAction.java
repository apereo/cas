package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.credential.UsernamePasswordCredential;
import org.apereo.cas.web.support.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apereo.inspektr.audit.annotation.Audit;
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
    
    @Audit(action = "AUTHENTICATION_EVENT",
        actionResolverName = "AUTHENTICATION_EVENT_ACTION_RESOLVER",
        resourceResolverName = "AUTHENTICATION_EVENT_RESOURCE_RESOLVER")
    @Override
    public Event doExecute(final RequestContext requestContext) {
        if (!WebUtils.hasMultiphaseAuthenticationUsername(requestContext) && isLoginFlowActive(requestContext)) {
            val username = requestContext.getRequestParameters().get("username");
            storeUsername(requestContext, username);
            return success();
        }
        return result(CasWebflowConstants.TRANSITION_ID_PROCEED);
        /*
        if (StringUtils.isBlank(username)) {
            val message = new MessageBuilder().error().code("TODO").build();
            messageContext.addMessage(message);
            return error();
        }
        WebUtils.putCredential(requestContext, new UsernamePasswordCredential(username, null));
        WebUtils.putMultiphaseAuthenticationUsername(requestContext, username);
        storeUsername(requestContext, username);
        return success();
        */
    }

    protected static boolean isLoginFlowActive(final RequestContext requestContext) {
        return requestContext.getActiveFlow().getId().equalsIgnoreCase(CasWebflowConfigurer.FLOW_ID_LOGIN);
    }

    protected void storeUsername(final RequestContext requestContext, final String username) {
        WebUtils.putCredential(requestContext, new UsernamePasswordCredential(username, null));
        WebUtils.putMultiphaseAuthenticationUsername(requestContext, username);
    }
}
