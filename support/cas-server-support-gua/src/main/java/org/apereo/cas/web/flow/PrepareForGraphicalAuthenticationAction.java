package org.apereo.cas.web.flow;

import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.flow.PrepareForMultiphaseAuthenticationAction;
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
public class PrepareForGraphicalAuthenticationAction extends PrepareForMultiphaseAuthenticationAction {

    public PrepareForGraphicalAuthenticationAction(final ServicesManager servicesManager) {
        super(servicesManager);
    }

    @Override
    public Event doExecute(final RequestContext requestContext) throws Exception {
        WebUtils.putGraphicalUserAuthenticationEnabled(requestContext, Boolean.TRUE);
        return super.doExecute(requestContext);
    }
}
