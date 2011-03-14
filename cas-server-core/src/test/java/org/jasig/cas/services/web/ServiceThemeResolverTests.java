/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
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
