/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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
public final class ServiceAuthorizationCheck extends AbstractAction {

	@NotNull
	private final ServicesManager servicesManager;

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public ServiceAuthorizationCheck(final ServicesManager servicesManager) {
		this.servicesManager = servicesManager;
	}

	@Override
	protected Event doExecute(final RequestContext context) throws Exception {
		final Service service = WebUtils.getService(context);
		//No service == plain /login request. Return success indicating transition to the login form
		if(service == null) {
			return success();
		}
		final RegisteredService registeredService = this.servicesManager.findServiceBy(service);

		if (registeredService == null) {
			logger.warn("Unauthorized Service Access for Service: [ {} ] - service is not defined in the service registry.", service.getId());
			throw new UnauthorizedServiceException();
		}
		else if (!registeredService.isEnabled()) {
			logger.warn("Unauthorized Service Access for Service: [ {} ] - service is not enabled in the service registry.", service.getId());
			throw new UnauthorizedServiceException();
		}

		return success();
	}
}