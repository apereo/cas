/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import org.jasig.cas.AbstractCentralAuthenticationServiceTest;
import org.jasig.cas.TestUtils;
import org.jasig.cas.web.flow.util.ContextUtils;
import org.jasig.cas.web.util.SecureCookieGenerator;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.validation.BindException;
import org.springframework.webflow.execution.servlet.ServletEvent;
import org.springframework.webflow.test.MockRequestContext;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.4
 *
 */
public class AuthenticationViaFormActionTests extends
    AbstractCentralAuthenticationServiceTest {

    private AuthenticationViaFormAction action;
    
    private SecureCookieGenerator cookieGenerator;
    
    private SecureCookieGenerator warnCookieGenerator;

    protected void onSetUp() throws Exception {
        this.action = new AuthenticationViaFormAction();
        
        this.cookieGenerator = new SecureCookieGenerator();
        this.cookieGenerator.setCookieName("TGT");
        this.cookieGenerator.setCookieDomain("/");
        this.cookieGenerator.setCookiePath("/");
        
        this.warnCookieGenerator = new SecureCookieGenerator();
        this.warnCookieGenerator.setCookieName("WARN");
        this.warnCookieGenerator.setCookieName("TGT");
        this.warnCookieGenerator.setCookieDomain("/");
        this.warnCookieGenerator.setCookiePath("/");
        
        this.action.setCentralAuthenticationService(getCentralAuthenticationService());
        this.action.setTicketGrantingTicketCookieGenerator(this.cookieGenerator);
        this.action.setWarnCookieGenerator(this.warnCookieGenerator);
        this.action.afterPropertiesSet();
    }
    
    public void testSuccessfulAuthenticationWithNoService() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockRequestContext context = new MockRequestContext();
        
        request.addParameter("username", "test");
        request.addParameter("password", "test");
          
        context.setSourceEvent(new ServletEvent(request, new MockHttpServletResponse()));
        ContextUtils.addAttribute(context, "credentials", TestUtils.getCredentialsWithSameUsernameAndPassword());
        assertEquals("success", this.action.submit(context).getId());
    }
    
    public void testSuccessfulAuthenticationWithNoServiceAndWarn() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockRequestContext context = new MockRequestContext();
        
        request.addParameter("username", "test");
        request.addParameter("password", "test");
        request.addParameter("warn", "true");
          
        context.setSourceEvent(new ServletEvent(request, response));
        ContextUtils.addAttribute(context, "credentials", TestUtils.getCredentialsWithSameUsernameAndPassword());
        assertEquals("success", this.action.submit(context).getId());
        assertNotNull(response.getCookie(this.warnCookieGenerator.getCookieName()));
    }
    
    public void testSuccessfulAuthenticationWithServiceAndWarn() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockRequestContext context = new MockRequestContext();
        
        request.addParameter("username", "test");
        request.addParameter("password", "test");
        request.addParameter("warn", "true");
        request.addParameter("service", "test");
          
        context.setSourceEvent(new ServletEvent(request, response));
        ContextUtils.addAttribute(context, "credentials", TestUtils.getCredentialsWithSameUsernameAndPassword());
        assertEquals("success", this.action.submit(context).getId());
        assertNotNull(response.getCookie(this.warnCookieGenerator.getCookieName()));
    }

    public void testFailedAuthenticationWithNoService() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockRequestContext context = new MockRequestContext();
        
        request.addParameter("username", "test");
        request.addParameter("password", "test2");
        
        context.setSourceEvent(new ServletEvent(request, new MockHttpServletResponse()));
        
        ContextUtils.addAttribute(context, "credentials", TestUtils.getCredentialsWithDifferentUsernameAndPassword());
        ContextUtils.addAttribute(context,
            "org.springframework.validation.BindException.credentials",
            new BindException(TestUtils
                .getCredentialsWithDifferentUsernameAndPassword(),
                "credentials"));
        
        
        assertEquals("error", this.action.submit(context).getId());
    }
}
