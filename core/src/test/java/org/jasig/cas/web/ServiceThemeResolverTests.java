/*
 * Copyright 2004 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web;

import org.jasig.cas.services.DefaultServiceRegistry;
import org.jasig.cas.services.RegisteredService;
import org.jasig.cas.services.ServiceRegistry;
import org.jasig.cas.services.ServiceRegistryManager;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import junit.framework.TestCase;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 *
 */
public class ServiceThemeResolverTests extends TestCase {
    private ServiceThemeResolver resolver;

    private ServiceRegistry registry;
    
    private RegisteredService service;
    
    private static final String DEFAULT_THEME = "default";

    protected void setUp() throws Exception {
        this.resolver = new ServiceThemeResolver();
        this.registry = new DefaultServiceRegistry();
        this.resolver.setDefaultThemeName(DEFAULT_THEME);
        this.resolver.setServiceRegistry(this.registry);
        
        this.service = new RegisteredService("test", false, false, "test", null, null);
        ((ServiceRegistryManager) this.registry).addService(this.service);
    }
    
    public void testSetThemeName() {
        this.resolver.setThemeName(new MockHttpServletRequest(), new MockHttpServletResponse(), "test");
    }
    
    public void testResolveThemeNameNonExistantService() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", "ohyeah");
        
        assertEquals(DEFAULT_THEME, this.resolver.resolveThemeName(request));
    }

    public void testResolveThemeNameExistingService() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", this.service.getId());
        
        assertEquals(this.service.getTheme(), this.resolver.resolveThemeName(request));
    }
    
    public void testResolveThemeNameNoRegistry() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("service", "ohyeah");
        this.resolver.setServiceRegistry(null);
        assertEquals(DEFAULT_THEME, this.resolver.resolveThemeName(request));
    }

}
