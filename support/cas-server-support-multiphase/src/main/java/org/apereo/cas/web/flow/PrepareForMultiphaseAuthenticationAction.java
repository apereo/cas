package org.apereo.cas.web.flow;

import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.flow.login.InitializeLoginAction;
import org.apereo.cas.web.support.WebUtils;

import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link PrepareForMultiphaseAuthenticationAction}.
 *
 * @author Hayden Sartoris
 * @since 6.2.0
 */
public class PrepareForMultiphaseAuthenticationAction extends InitializeLoginAction {
	public PrepareForMultiphaseAuthenticationAction(final ServicesManager servicesManager) {
		super(servicesManager);
	}

	@Override
	public Event doExecute(final RequestContext requestContext) throws Exception {
		WebUtils.putMultiphaseAuthenticationEnabled(requestContext, Boolean.TRUE);
		if (!WebUtils.hasMultiphaseAuthenticationUsername(requestContext)) {
			return new EventFactorySupport().event(this, 
					MultiphaseAuthenticationWebflowConfigurer.TRANSITION_ID_MULTIPHASE_GET_USERID);
		}
		return super.doExecute(requestContext);
	}
}
