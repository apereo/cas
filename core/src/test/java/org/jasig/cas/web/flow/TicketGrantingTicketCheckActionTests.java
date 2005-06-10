/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import javax.servlet.http.Cookie;

import org.jasig.cas.CentralAuthenticationServiceImpl;
import org.jasig.cas.authentication.AuthenticationManagerImpl;
import org.jasig.cas.authentication.handler.AuthenticationHandler;
import org.jasig.cas.authentication.handler.support.SimpleTestUsernamePasswordAuthenticationHandler;
import org.jasig.cas.authentication.principal.CredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentialsToPrincipalResolver;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.ticket.registry.DefaultTicketRegistry;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.jasig.cas.util.DefaultUniqueTicketIdGenerator;
import org.jasig.cas.web.support.WebConstants;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.flow.MockRequestContext;
import org.springframework.web.flow.execution.servlet.ServletEvent;

import junit.framework.TestCase;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 *
 */
public class TicketGrantingTicketCheckActionTests extends TestCase {

    private TicketGrantingTicketCheckAction checkAction = new TicketGrantingTicketCheckAction();
    
    private CentralAuthenticationServiceImpl centralAuthenticationService;
    
    protected void setUp() throws Exception {
        this.centralAuthenticationService = new CentralAuthenticationServiceImpl();
        this.centralAuthenticationService.setTicketRegistry(new DefaultTicketRegistry());
                
        AuthenticationManagerImpl manager = new AuthenticationManagerImpl();
        manager.setAuthenticationHandlers(new AuthenticationHandler[] {new SimpleTestUsernamePasswordAuthenticationHandler()});
        manager.setCredentialsToPrincipalResolvers(new CredentialsToPrincipalResolver[] {new UsernamePasswordCredentialsToPrincipalResolver()});
        manager.afterPropertiesSet();
        
        this.centralAuthenticationService.setAuthenticationManager(manager);
        this.centralAuthenticationService.setServiceTicketExpirationPolicy(new NeverExpiresExpirationPolicy());
        this.centralAuthenticationService.setTicketGrantingTicketExpirationPolicy(new NeverExpiresExpirationPolicy());
        this.centralAuthenticationService.setTicketGrantingTicketUniqueTicketIdGenerator(new DefaultUniqueTicketIdGenerator());
        this.centralAuthenticationService.setServiceTicketUniqueTicketIdGenerator(new DefaultUniqueTicketIdGenerator());
        this.checkAction.setCentralAuthenticationService(this.centralAuthenticationService);
        this.checkAction.afterPropertiesSet();
    }
    
    public void testCheckNoTicketGrantingTicketCookie() throws Exception {
        MockRequestContext context = new MockRequestContext();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        ServletEvent event = new ServletEvent(request, response);
        context.setSourceEvent(event);
        request.addParameter(WebConstants.SERVICE, "test");
        
        assertEquals("error", this.checkAction.doExecute(context).getId());
    }
    
    public void testCheckInvalidTicketGrantingTicketCookie() throws Exception {
        MockRequestContext context = new MockRequestContext();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        ServletEvent event = new ServletEvent(request, response);
        context.setSourceEvent(event);
        request.addParameter(WebConstants.SERVICE, "test");
        Cookie cookie = new Cookie(WebConstants.COOKIE_TGC_ID, "test");
        request.setCookies(new Cookie[] {cookie});
        
        assertEquals("error", this.checkAction.doExecute(context).getId());
    }
    
    public void testCheckInvalidTicketGrantingTicketCookieWithGateway() throws Exception {
        MockRequestContext context = new MockRequestContext();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        ServletEvent event = new ServletEvent(request, response);
        context.setSourceEvent(event);
        request.addParameter(WebConstants.SERVICE, "test");
        request.addParameter(WebConstants.GATEWAY, "true");
        Cookie cookie = new Cookie(WebConstants.COOKIE_TGC_ID, "test");
        request.setCookies(new Cookie[] {cookie});
        
        assertEquals("gateway", this.checkAction.doExecute(context).getId());
    }
    
    public void testCheckRenewTrue() throws Exception {
        MockRequestContext context = new MockRequestContext();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        ServletEvent event = new ServletEvent(request, response);
        context.setSourceEvent(event);
        request.addParameter(WebConstants.RENEW, "true");
        request.addParameter(WebConstants.SERVICE, "test");        
        
        assertEquals("error", this.checkAction.doExecute(context).getId());
    }
    
    public void testCheckValidTicketGrantingTicketCookie() throws Exception {
        MockRequestContext context = new MockRequestContext();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        ServletEvent event = new ServletEvent(request, response);
        context.setSourceEvent(event);
        request.addParameter(WebConstants.SERVICE, "test");
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials();
        credentials.setUsername("test");
        credentials.setPassword("test");
        Cookie cookie = new Cookie(WebConstants.COOKIE_TGC_ID, this.centralAuthenticationService.createTicketGrantingTicket(credentials));
        request.setCookies(new Cookie[] {cookie});
        
        assertEquals("success", this.checkAction.doExecute(context).getId());
    }
    
    public void testValidTgtNoService() throws Exception {
        MockRequestContext context = new MockRequestContext();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        ServletEvent event = new ServletEvent(request, response);
        context.setSourceEvent(event);
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials();
        credentials.setUsername("test");
        credentials.setPassword("test");
        Cookie cookie = new Cookie(WebConstants.COOKIE_TGC_ID, this.centralAuthenticationService.createTicketGrantingTicket(credentials));
        request.setCookies(new Cookie[] {cookie});
        
        assertEquals("noService", this.checkAction.doExecute(context).getId());
    }
    
    public void testNoTgtAndGateway() throws Exception {
        MockRequestContext context = new MockRequestContext();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        ServletEvent event = new ServletEvent(request, response);
        context.setSourceEvent(event);
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials();
        request.addParameter(WebConstants.GATEWAY, "true");
        request.addParameter(WebConstants.SERVICE, "test");
        credentials.setUsername("test");
        credentials.setPassword("test");
        
        assertEquals("gateway", this.checkAction.doExecute(context).getId());
    }
    
    public void testAfterPropertiesSet() {
        this.checkAction.setCentralAuthenticationService(null);
        try {
            this.checkAction.afterPropertiesSet();
            fail("exception expected.");
        } catch (Exception e) {
            return;
        }
    }
}
