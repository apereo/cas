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

import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.services.RegisteredServiceImpl;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.services.UnauthorizedServiceException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Mockito based tests for @{link ServiceAuthorizationCheck}
 *
 * @author Dmitriy Kopylenko
 */
public class ServiceAuthorizationCheckTests {

	private ServiceAuthorizationCheck serviceAuthorizationCheck;

	private WebApplicationService authorizedService = mock(WebApplicationService.class);

	private WebApplicationService unauthorizedService = mock(WebApplicationService.class);

	private WebApplicationService undefinedService = mock(WebApplicationService.class);

	private ServicesManager servicesManager = mock(ServicesManager.class);


	@Before
	public void setUpMocks() {
		RegisteredServiceImpl authorizedRegisteredService = new RegisteredServiceImpl();
		RegisteredServiceImpl unauthorizedRegisteredService = new RegisteredServiceImpl();
		unauthorizedRegisteredService.setEnabled(false);

		when(this.servicesManager.findServiceBy(this.authorizedService)).thenReturn(authorizedRegisteredService);
		when(this.servicesManager.findServiceBy(this.unauthorizedService)).thenReturn(unauthorizedRegisteredService);
		when(this.servicesManager.findServiceBy(this.undefinedService)).thenReturn(null);
		this.serviceAuthorizationCheck = new ServiceAuthorizationCheck(this.servicesManager);
	}

	@Test
	public void noServiceProvided() throws Exception {
		MockRequestContext mockRequestContext = new MockRequestContext();
		Event event = this.serviceAuthorizationCheck.doExecute(mockRequestContext);
		assertEquals("success", event.getId());

	}

	@Test
	public void authorizedServiceProvided() throws Exception {
		MockRequestContext mockRequestContext = new MockRequestContext();
		mockRequestContext.getFlowScope().put("service", this.authorizedService);
		Event event = this.serviceAuthorizationCheck.doExecute(mockRequestContext);
		assertEquals("success", event.getId());
	}

	@Test
	public void unauthorizedServiceProvided() throws Exception {
		MockRequestContext mockRequestContext = new MockRequestContext();
		mockRequestContext.getFlowScope().put("service", this.unauthorizedService);
		try {
			this.serviceAuthorizationCheck.doExecute(mockRequestContext);
			fail("Should have thrown UnauthorizedServiceException");
		}
		catch (UnauthorizedServiceException e) {
			//expected
		}

	}

	@Test
	public void serviceThatIsNotRegisteredProvided() throws Exception {
		MockRequestContext mockRequestContext = new MockRequestContext();
		mockRequestContext.getFlowScope().put("service", this.undefinedService);
		try {
			this.serviceAuthorizationCheck.doExecute(mockRequestContext);
			fail("Should have thrown UnauthorizedServiceException");
		}
		catch (UnauthorizedServiceException e) {
			//expected
		}
	}
}
