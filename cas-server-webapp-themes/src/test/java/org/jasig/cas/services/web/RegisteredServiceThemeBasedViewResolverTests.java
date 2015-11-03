/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
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
package org.jasig.cas.services.web;

import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.authentication.principal.WebApplicationServiceFactory;
import org.jasig.cas.services.DefaultServicesManagerImpl;
import org.jasig.cas.services.InMemoryServiceRegistryDaoImpl;
import org.jasig.cas.services.RegisteredServiceImpl;
import org.jasig.cas.services.ServicesManager;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.webflow.execution.RequestContextHolder;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author John Gasper
 * @since 4.1.0
 *
 */
public class RegisteredServiceThemeBasedViewResolverTests {

    private RegisteredServiceThemeBasedViewResolver registeredServiceThemeBasedViewResolver;

    private DefaultServicesManagerImpl servicesManager;

    @Before
    public void setUp() throws Exception {
        this.servicesManager = new DefaultServicesManagerImpl(new InMemoryServiceRegistryDaoImpl());
        this.servicesManager.setApplicationEventPublisher(mock(ApplicationEventPublisher.class));
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setTheme("myTheme");
        r.setId(1000);
        r.setName("Test Service");
        r.setServiceId("myServiceId");
        this.servicesManager.save(r);

        final RegisteredServiceImpl r2 = new RegisteredServiceImpl();
        r2.setTheme(null);
        r2.setId(1001);
        r2.setName("Test Service 2");
        r2.setServiceId("myDefaultId");
        this.servicesManager.save(r2);

        this.registeredServiceThemeBasedViewResolver = new RegisteredServiceThemeBasedViewResolver(this.servicesManager);
        this.registeredServiceThemeBasedViewResolver.setPrefix("/WEB-INF/view/jsp");
    }

    @Test
    public void verifyGetServiceWithTheme() throws Exception {
        final MockRequestContext requestContext = new MockRequestContext();
        RequestContextHolder.setRequestContext(requestContext);

        final WebApplicationService webApplicationService = new WebApplicationServiceFactory().createService("myServiceId");
        requestContext.getFlowScope().put("service", webApplicationService);

        assertEquals("/WEB-INF/view/jsp/myTheme/ui/casLoginView",
                this.registeredServiceThemeBasedViewResolver.buildView("casLoginView").getUrl());
    }

    @Test
    public void verifyGetServiceWithDefault() throws Exception {
        final MockRequestContext requestContext = new MockRequestContext();
        RequestContextHolder.setRequestContext(requestContext);

        final WebApplicationService webApplicationService = new WebApplicationServiceFactory().createService("myDefaultId");
        requestContext.getFlowScope().put("service", webApplicationService);

        assertEquals("/WEB-INF/view/jsp/default/ui/casLoginView",
                this.registeredServiceThemeBasedViewResolver.buildView("casLoginView").getUrl());
    }

    @Test
    public void verifyNoService() throws Exception {
        final MockRequestContext requestContext = new MockRequestContext();
        RequestContextHolder.setRequestContext(requestContext);

        assertEquals("/WEB-INF/view/jsp/default/ui/casLoginView",
                this.registeredServiceThemeBasedViewResolver.buildView("casLoginView").getUrl());
    }
}
