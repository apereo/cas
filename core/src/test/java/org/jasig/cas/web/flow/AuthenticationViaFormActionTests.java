/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.AbstractCentralAuthenticationServiceTest;
import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.principal.Credentials;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.web.bind.CredentialsBinder;
import org.jasig.cas.web.flow.util.ContextUtils;
import org.jasig.cas.web.util.SecureCookieGenerator;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.validation.BindException;
import org.springframework.webflow.Event;
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
    
    public void testRenewWithServiceAndSameCredentials() throws Exception {
        final String ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(TestUtils.getCredentialsWithSameUsernameAndPassword());
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockRequestContext context = new MockRequestContext();
        
        request.setCookies(new Cookie[] {new Cookie("TGT", ticketGrantingTicket)});
        request.addParameter("renew", "true");
        request.addParameter("service", "test");
        
        context.setSourceEvent(new ServletEvent(request, new MockHttpServletResponse()));
        ContextUtils.addAttribute(context, "credentials", TestUtils.getCredentialsWithSameUsernameAndPassword());
        ContextUtils.addAttribute(context,
            "org.springframework.validation.BindException.credentials",
            new BindException(TestUtils
                .getCredentialsWithSameUsernameAndPassword(),
                "credentials"));
        
        assertEquals("warn", this.action.submit(context).getId());
    }
    
    public void testRenewWithServiceAndDifferentCredentials() throws Exception {
        final String ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(TestUtils.getCredentialsWithSameUsernameAndPassword());
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockRequestContext context = new MockRequestContext();
        
        request.setCookies(new Cookie[] {new Cookie("TGT", ticketGrantingTicket)});
        request.addParameter("renew", "true");
        request.addParameter("service", "test");
        
        context.setSourceEvent(new ServletEvent(request, new MockHttpServletResponse()));
        ContextUtils.addAttribute(context, "credentials", TestUtils.getCredentialsWithSameUsernameAndPassword("test2"));
        ContextUtils.addAttribute(context,
            "org.springframework.validation.BindException.credentials",
            new BindException(TestUtils.getCredentialsWithSameUsernameAndPassword("test2"),
                "credentials"));
        
        assertEquals("success", this.action.submit(context).getId());
    }
    
    public void testRenewWithServiceAndBadCredentials() throws Exception {
        final String ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(TestUtils.getCredentialsWithSameUsernameAndPassword());
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final MockRequestContext context = new MockRequestContext();
        
        request.setCookies(new Cookie[] {new Cookie("TGT", ticketGrantingTicket)});
        request.addParameter("renew", "true");
        request.addParameter("service", "test");
        
        context.setSourceEvent(new ServletEvent(request, new MockHttpServletResponse()));
        ContextUtils.addAttribute(context, "credentials", TestUtils.getCredentialsWithDifferentUsernameAndPassword());
        ContextUtils.addAttribute(context,
            "org.springframework.validation.BindException.credentials",
            new BindException(TestUtils.getCredentialsWithDifferentUsernameAndPassword(),
                "credentials"));
        
        assertEquals("error", this.action.submit(context).getId());
    }
    
    public void testTestBindingWithoutCredentialsBinder()  throws Exception{
        final MockRequestContext context = new MockRequestContext();
        Event event = new ServletEvent(new MockHttpServletRequest(), new MockHttpServletResponse());
        context.setSourceEvent(event);
        context.setLastEvent(event);
        ContextUtils.addAttribute(context, "credentials", TestUtils.getCredentialsWithSameUsernameAndPassword());
        ContextUtils.addAttribute(context,
            "org.springframework.validation.BindException.credentials",
            new BindException(TestUtils.getCredentialsWithSameUsernameAndPassword(),
                "credentials"));
        
        assertEquals("success", this.action.bindAndValidate(context).getId());

    }
    
    public void testTestBindingWithCredentialsBinder()  throws Exception{
        final MockRequestContext context = new MockRequestContext();
        Event event = new ServletEvent(new MockHttpServletRequest(), new MockHttpServletResponse());
        context.setSourceEvent(event);
        context.setLastEvent(event);
        UsernamePasswordCredentials c = TestUtils.getCredentialsWithSameUsernameAndPassword();
        ContextUtils.addAttribute(context, "credentials", c);
        ContextUtils.addAttribute(context,
            "org.springframework.validation.BindException.credentials",
            new BindException(TestUtils.getCredentialsWithSameUsernameAndPassword(),
                "credentials"));
        
        final CredentialsBinder cb = new CredentialsBinder(){

            public void bind(HttpServletRequest request, Credentials credentials) {
                ((UsernamePasswordCredentials) credentials).setUsername("test2");
            }

            public boolean supports(Class clazz) {
                return true;
            }
            
        };
        this.action.setCredentialsBinder(cb);
        this.action.bindAndValidate(context);
        
        assertEquals("test2", c.getUsername());

    }
    
    public void testSetCredentialsBinderWithFailure() {
        final CredentialsBinder c = new CredentialsBinder() {

            public void bind(final HttpServletRequest request, final Credentials credentials) {
                // TODO nothing to do here
            }

            public boolean supports(final Class clazz) {
                return false;
            }            
        };
        
        try {
            this.action.setCredentialsBinder(c);
            this.action.afterPropertiesSet();
            fail("Exception expected.");
        } catch (IllegalStateException e) {
            // this is okay
        }
    }
    
    public void testSetCredentialsBinderNoFailure() {
        final CredentialsBinder c = new CredentialsBinder() {

            public void bind(final HttpServletRequest request, final Credentials credentials) {
                // TODO nothing to do here
            }

            public boolean supports(final Class clazz) {
                return true;
            }            
        };
    
        this.action.setCredentialsBinder(c);
        this.action.afterPropertiesSet();
    }
}
