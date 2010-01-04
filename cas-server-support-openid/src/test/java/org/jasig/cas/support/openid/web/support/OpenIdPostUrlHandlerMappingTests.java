/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.support.openid.web.support;

import junit.framework.TestCase;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.support.GenericWebApplicationContext;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 *
 */
public class OpenIdPostUrlHandlerMappingTests extends TestCase {

    private OpenIdPostUrlHandlerMapping handlerMapping;

    public void testTest() {
    }

 /*
    protected void setUp() throws Exception {
        final GenericWebApplicationContext context = new GenericWebApplicationContext();
        context.refresh();
        final RootBeanDefinition definition = new RootBeanDefinition(Object.class);
        context.registerBeanDefinition("testHandler", definition);

        context.start();

        final Map<String, Object> properties = new HashMap<String, Object>();
        properties.put("/login", new Object());

        this.handlerMapping = new OpenIdPostUrlHandlerMapping();
        this.handlerMapping.setUrlMap(properties);

        this.handlerMapping.initApplicationContext();
    }


    public void testNoMatch() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/hello");
        
        assertNull(this.handlerMapping.lookupHandler("/hello", request));
    }
    
    public void testImproperMatch() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/hello");
        
        assertNull(this.handlerMapping.lookupHandler("/login", request));
    }
    
    public void testProperMatchWrongMethod() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/login");
        request.setMethod("GET");
        
        assertNull(this.handlerMapping.lookupHandler("/login", request));
    }
    
    public void testProperMatchCorrectMethodNoParam() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/login");
        request.setMethod("POST");
        
        assertNull(this.handlerMapping.lookupHandler("/login", request));
    }
    
    public void testProperMatchCorrectMethodWithParam() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/login");
        request.setMethod("POST");
        request.setParameter("openid.mode", "check_authentication");
        
        
        assertNotNull(this.handlerMapping.lookupHandler("/login", request));
    }*/
}
