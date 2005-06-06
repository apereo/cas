/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import javax.servlet.http.Cookie;

import org.jasig.cas.web.support.WebConstants;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.flow.MockRequestContext;
import org.springframework.web.flow.Event;
import org.springframework.web.flow.execution.servlet.ServletEvent;

import junit.framework.TestCase;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 *
 */
public class WarnActionTests extends TestCase {
    private WarnAction warnAction = new WarnAction();

    public void testWarnFromCookie() throws Exception {
        MockRequestContext context = new MockRequestContext();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        ServletEvent event = new ServletEvent(request, response);
        context.setSourceEvent(event);
        Cookie cookie = new Cookie(WebConstants.COOKIE_PRIVACY, WebConstants.COOKIE_DEFAULT_FILLED_VALUE);
        request.setCookies(new Cookie[] {cookie});
        
        Event finalEvent = this.warnAction.doExecute(context);
        
        assertEquals("warn", finalEvent.getId());
    }
    
    public void testWarnFromRequestParameter() throws Exception {
        MockRequestContext context = new MockRequestContext();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        ServletEvent event = new ServletEvent(request, response);
        context.setSourceEvent(event);
        request.addParameter(WebConstants.WARN, "true");
        
        Event finalEvent = this.warnAction.doExecute(context);
        
        assertEquals("warn", finalEvent.getId()); 
    }
    
    public void testNoWarn() throws Exception {
        MockRequestContext context = new MockRequestContext();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        ServletEvent event = new ServletEvent(request, response);
        context.setSourceEvent(event);
        
        Event finalEvent = this.warnAction.doExecute(context);
        
        assertEquals("redirect", finalEvent.getId()); 
    }
}
