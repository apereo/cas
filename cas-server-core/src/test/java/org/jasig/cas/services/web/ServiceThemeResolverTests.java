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
package org.jasig.cas.services.web;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.jasig.cas.services.DefaultServicesManagerImpl;
import org.jasig.cas.services.InMemoryServiceRegistryDaoImpl;
import org.jasig.cas.services.RegisteredServiceImpl;
import org.jasig.cas.services.ServicesManager;
import org.jasig.cas.web.support.ArgumentExtractor;
import org.jasig.cas.web.support.CasArgumentExtractor;
import org.springframework.mock.web.MockHttpServletRequest;

import junit.framework.TestCase;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 *
 */
public class ServiceThemeResolverTests extends TestCase {
    
    private ServiceThemeResolver serviceThemeResolver;
    
    private ServicesManager servicesManager;

    protected void setUp() throws Exception {
        this.servicesManager = new DefaultServicesManagerImpl(new InMemoryServiceRegistryDaoImpl());
        
        this.serviceThemeResolver = new ServiceThemeResolver();
        this.serviceThemeResolver.setDefaultThemeName("test");
        this.serviceThemeResolver.setServicesManager(this.servicesManager);
        this.serviceThemeResolver.setArgumentExtractors(Arrays.asList((ArgumentExtractor) new CasArgumentExtractor()));
        final Map<String, String> mobileBrowsers = new HashMap<String, String>();
        mobileBrowsers.put("Mozilla", "theme");
        this.serviceThemeResolver.setMobileBrowsers(mobileBrowsers);
    }
    
    public void testGetServiceTheme() {
        final RegisteredServiceImpl r = new RegisteredServiceImpl();
        r.setTheme("myTheme");
        r.setId(1000);
        r.setServiceId("myServiceId");
        
        this.servicesManager.save(r);
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("service", "myServiceId");
        request.addHeader("User-Agent", "Mozilla");
        System.out.println("1");
        assertEquals("myTheme", this.serviceThemeResolver.resolveThemeName(request));
    }
    
    public void testGetDefaultService() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("service", "myServiceId");
        request.addHeader("User-Agent", "Mozilla");
        assertEquals("test", this.serviceThemeResolver.resolveThemeName(request));
    }
    
    public void testGetDefaultServiceWithNoServicesManager() {
        this.serviceThemeResolver.setServicesManager(null);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("service", "myServiceId");
        request.addHeader("User-Agent", "Mozilla");
        assertEquals("test", this.serviceThemeResolver.resolveThemeName(request));
    }
    
    

}
