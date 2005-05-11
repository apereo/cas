/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow.util;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.flow.MockRequestContext;
import org.springframework.web.flow.SimpleEvent;
import org.springframework.web.flow.execution.servlet.HttpServletRequestEvent;

import junit.framework.TestCase;


public class ContextUtilsTests extends TestCase {

    private MockRequestContext requestContext;
    
    private MockHttpServletRequest request;
    
    private MockHttpServletResponse response;
    
    
    
    protected void setUp() throws Exception {
        this.request = new MockHttpServletRequest();
        this.response = new MockHttpServletResponse();
        this.requestContext = new MockRequestContext();
        HttpServletRequestEvent event = new HttpServletRequestEvent(this.request, this.response);
        this.requestContext.setOriginatingEvent(event);
    }

    public void testAddGetAttribute() {
        ContextUtils.addAttribute(this.requestContext, "test", "test");
        assertEquals("test", (String) ContextUtils.getAttribute(this.requestContext, "test"));
    }
    
    public void testGetHttpServletRequest() {
        assertEquals(this.request, ContextUtils.getHttpServletRequest(this.requestContext));
    }
   
    public void testGetHttpServletResponse() {
        assertEquals(this.response, ContextUtils.getHttpServletResponse(this.requestContext));
    }
    
    public void testGetHttpServletRequestThrowsException() {
        this.requestContext.setOriginatingEvent(new SimpleEvent("test"));
        try {
            ContextUtils.getHttpServletRequest(this.requestContext);
            fail("Exception expected.");
        } catch (Exception e) {
            return;
        }
    }
    
    public void testGetHttpServletResponseThrowsException() {
        this.requestContext.setOriginatingEvent(new SimpleEvent("test"));
        try {
            ContextUtils.getHttpServletResponse(this.requestContext);
            fail("Exception expected.");
        } catch (Exception e) {
            return;
        }
    }
}
