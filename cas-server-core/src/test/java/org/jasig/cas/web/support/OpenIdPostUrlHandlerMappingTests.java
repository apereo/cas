/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.support;

import java.util.Properties;

import org.springframework.mock.web.MockHttpServletRequest;

import junit.framework.TestCase;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 *
 */
public class OpenIdPostUrlHandlerMappingTests extends TestCase {

    private OpenIdPostUrlHandlerMapping handlerMapping;

    protected void setUp() throws Exception {
        this.handlerMapping = new OpenIdPostUrlHandlerMapping();
        final Properties properties = new Properties();
        
        properties.setProperty("/login", "testHandler");
        
        this.handlerMapping.setMappings(properties);
        super.setUp();
    }
    
    public void testNoMatch() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/hello");
        
        assertNull(this.handlerMapping.lookupHandler("/hello", request));
    }
    
    public void testImproperMatch() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/hello");
        
        assertNull(this.handlerMapping.lookupHandler("/login", request));
    }
    
    public void testProperMatchWrongMethod() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/login");
        request.setMethod("GET");
        
        assertNull(this.handlerMapping.lookupHandler("/login", request));
    }
    
    public void testProperMatchCorrectMethodNoParam() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/login");
        request.setMethod("POST");
        
        assertNull(this.handlerMapping.lookupHandler("/login", request));
    }
    
    // TODO fix this test
    /*
    public void testProperMatchCorrectMethodWithParam() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("/login");
        request.setMethod("POST");
        request.setParameter("openid.mode", "check_authentication");
        
        assertNotNull(this.handlerMapping.lookupHandler("/login", request));
    }*/
}
