package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.model.support.gua.GraphicalUserAuthenticationProperties;
import org.apereo.cas.gua.api.UserGraphicalAuthenticationRepository;
import org.apereo.cas.services.ServicesManager;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link PrepareForGraphicalAuthenticationAction}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class PrepareForGraphicalAuthenticationAction extends InitializeLoginAction {
    private final GraphicalUserAuthenticationProperties guaProperties;
    private final UserGraphicalAuthenticationRepository repository;
    
    public PrepareForGraphicalAuthenticationAction(final ServicesManager servicesManager,
                                                   final GraphicalUserAuthenticationProperties guaProperties,
                                                   final UserGraphicalAuthenticationRepository repository) {
        super(servicesManager);
        this.guaProperties = guaProperties;
        this.repository = repository;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        requestContext.getFlowScope().put("guaEnabled", true);
        if (!requestContext.getFlowScope().contains("guaUsername")) {
            return new EventFactorySupport().event(this, GraphicalUserAuthenticationWebflowConfigurer.TRANSITION_ID_GUA_GET_USERID);
        }
        return super.doExecute(requestContext);
    }
}
