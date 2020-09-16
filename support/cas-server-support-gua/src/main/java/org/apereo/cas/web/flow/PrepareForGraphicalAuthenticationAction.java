package org.apereo.cas.web.flow;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.flow.login.InitializeLoginAction;
import org.apereo.cas.web.support.WebUtils;

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

    public PrepareForGraphicalAuthenticationAction(final ServicesManager servicesManager,
        final CasConfigurationProperties casProperties) {
        super(servicesManager, casProperties);
    }

    @Override
    public Event doExecute(final RequestContext requestContext) throws Exception {
        WebUtils.putGraphicalUserAuthenticationEnabled(requestContext, Boolean.TRUE);
        if (!WebUtils.containsGraphicalUserAuthenticationUsername(requestContext)) {
            return new EventFactorySupport().event(this, GraphicalUserAuthenticationWebflowConfigurer.TRANSITION_ID_GUA_GET_USERID);
        }
        return super.doExecute(requestContext);
    }
}
