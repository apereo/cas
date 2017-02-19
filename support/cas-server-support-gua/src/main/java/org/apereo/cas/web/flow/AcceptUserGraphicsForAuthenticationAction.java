package org.apereo.cas.web.flow;

import org.apereo.cas.authentication.UsernamePasswordCredential;
import org.apereo.cas.configuration.model.support.gua.GraphicalUserAuthenticationProperties;
import org.apereo.cas.gua.api.UserGraphicalAuthenticationRepository;
import org.apereo.cas.services.ServicesManager;
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
    private final GraphicalUserAuthenticationProperties guaProperties;
    private final ServicesManager servicesManager;
    private final UserGraphicalAuthenticationRepository repository;

    public AcceptUserGraphicsForAuthenticationAction(final ServicesManager servicesManager,
                                                     final GraphicalUserAuthenticationProperties guaProperties,
                                                     final UserGraphicalAuthenticationRepository repository) {
        this.guaProperties = guaProperties;
        this.servicesManager = servicesManager;
        this.repository = repository;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        final String username = requestContext.getRequestParameters().get("username");
        WebUtils.putCredential(requestContext, new UsernamePasswordCredential(username, null));
        requestContext.getFlowScope().put("guaUsername", username);
        return success();
    }
}
