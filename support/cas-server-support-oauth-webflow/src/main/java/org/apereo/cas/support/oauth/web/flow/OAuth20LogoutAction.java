package org.apereo.cas.support.oauth.web.flow;

import org.apereo.cas.web.support.WebUtils;
import org.pac4j.springframework.web.ApplicationLogoutController;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link OAuth20LogoutAction} that destroys the oauth session.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class OAuth20LogoutAction extends AbstractAction {
    
    private ApplicationLogoutController applicationLogoutController;
    
    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        this.applicationLogoutController.applicationLogout(WebUtils.getHttpServletRequest(requestContext),
                WebUtils.getHttpServletResponse(requestContext));
        return success();
    }

    public void setApplicationLogoutController(final ApplicationLogoutController applicationLogoutController) {
        this.applicationLogoutController = applicationLogoutController;
    }
}
