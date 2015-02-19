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

import org.jasig.cas.TestUtils;
import org.jasig.cas.services.DefaultServicesManagerImpl;
import org.jasig.cas.services.InMemoryServiceRegistryDaoImpl;
import org.jasig.cas.services.RegisteredServiceImpl;
import org.jasig.cas.services.ServicesManager;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.webflow.core.collection.LocalAttributeMap;
import org.springframework.webflow.core.collection.MutableAttributeMap;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author Scott Battaglia
 * @since 3.1
 *
 */
public class ServiceThemeResolverTests {

    private ServiceThemeResolver serviceThemeResolver;

    private ServicesManager servicesManager;

    @Before
    public void setUp() throws Exception {
        this.servicesManager = new DefaultServicesManagerImpl(new InMemoryServiceRegistryDaoImpl());

        this.serviceThemeResolver = new ServiceThemeResolver();
        this.serviceThemeResolver.setDefaultThemeName("test");
        this.serviceThemeResolver.setServicesManager(this.servicesManager);
        final Map<String, String> mobileBrowsers = new HashMap<>();
        mobileBrowsers.put("Mozilla", "theme");
        this.serviceThemeResolver.setMobileBrowsers(mobileBrowsers);
    }

    @Test
    public void verifyGetServiceThemeDoesNotExist() {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setTheme("myTheme");
        r.setId(1000);
        r.setName("Test Service");
        r.setServiceId("myServiceId");

        this.servicesManager.save(r);

        final MockHttpServletRequest request = new MockHttpServletRequest();
        final RequestContext ctx = mock(RequestContext.class);
        final MutableAttributeMap scope = new LocalAttributeMap();
        scope.put("service", TestUtils.getService(r.getServiceId()));
        when(ctx.getFlowScope()).thenReturn(scope);
        RequestContextHolder.setRequestContext(ctx);
        request.addHeader("User-Agent", "Mozilla");
        assertEquals("test", this.serviceThemeResolver.resolveThemeName(request));
    }

    @Test
    public void verifyGetDefaultService() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("service", "myServiceId");
        request.addHeader("User-Agent", "Mozilla");
        assertEquals("test", this.serviceThemeResolver.resolveThemeName(request));
    }

    @Test
    public void verifyGetDefaultServiceWithNoServicesManager() {
        this.serviceThemeResolver.setServicesManager(null);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("service", "myServiceId");
        request.addHeader("User-Agent", "Mozilla");
        assertEquals("test", this.serviceThemeResolver.resolveThemeName(request));
    }



}
