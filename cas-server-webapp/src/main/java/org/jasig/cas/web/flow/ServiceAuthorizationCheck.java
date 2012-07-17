package org.jasig.cas.web.flow;

import org.jasig.cas.authentication.principal.Service;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.services.UnauthorizedServiceException;
import org.jasig.cas.web.support.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.test.MockRequestContext;

import javax.validation.constraints.NotNull;

/**
 * Performs a basic check if an authentication request for a provided service is authorized to proceed
 * based on the registered services registry configuration (or lack thereof)
 *
 * @author Dmitriy Kopylenko
 * @since 3.5.1
 */
public class ServiceAuthorizationCheck extends AbstractAction {

	@NotNull
	private final ServicesManager servicesManager;

	private static final Logger logger = LoggerFactory.getLogger(ServiceAuthorizationCheck.class);

	public ServiceAuthorizationCheck(ServicesManager servicesManager) {
		this.servicesManager = servicesManager;
	}

	@Override
	protected Event doExecute(RequestContext context) throws Exception {
		final Service service = WebUtils.getService(context);
		//No service == plian /login request. Return success indicating transition to the login form
		if(service == null) {
			return success();
		}
		final RegisteredService registeredService = this.servicesManager.findServiceBy(service);

		if (registeredService == null || !registeredService.isEnabled()) {
			logger.warn("Unauthorized Service Access for Service: [ {} ]", service.getId());
			throw new UnauthorizedServiceException();
		}

		return success();
	}
}
