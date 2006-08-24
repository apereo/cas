/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import javax.servlet.http.Cookie;

import org.jasig.cas.AbstractCentralAuthenticationServiceTest;
import org.jasig.cas.TestUtils;
import org.jasig.cas.web.CasArgumentExtractor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.4
 *
 */
public final class GenerateServiceTicketActionTests extends
    AbstractCentralAuthenticationServiceTest {
    
    private GenerateServiceTicketAction action;
    
    private String ticketGrantingTicket;
    
    private CookieGenerator tgtCookieGenerator;
    
    private CookieGenerator warnCookieGenerator;
    
    private CasArgumentExtractor casArgumentExtractor;

    protected void onSetUp() throws Exception {
        this.action = new GenerateServiceTicketAction();
        this.tgtCookieGenerator = new CookieGenerator();
        this.tgtCookieGenerator.setCookieName("TGT");
        
        this.warnCookieGenerator = new CookieGenerator();
        this.warnCookieGenerator.setCookieName("WARN");
        
        this.action.setCentralAuthenticationService(getCentralAuthenticationService());
        this.casArgumentExtractor = new CasArgumentExtractor(this.tgtCookieGenerator, this.warnCookieGenerator);
        this.action.setCasArgumentExtractor(this.casArgumentExtractor);
        
        this.ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(TestUtils.getCredentialsWithSameUsernameAndPassword());
    }
    
    public void testServiceTicketFromCookie() throws Exception {
        MockRequestContext context = new MockRequestContext();
        MockHttpServletRequest request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        request.addParameter("service", "service");
        request.setCookies(new Cookie[] {new Cookie("TGT", this.ticketGrantingTicket)});
        
        this.action.execute(context);
        
        assertNotNull(this.casArgumentExtractor.getServiceTicketFrom(context));
    }
    
    public void testTicketGrantingTicketFromRequest() throws Exception {
        MockRequestContext context = new MockRequestContext();
        MockHttpServletRequest request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        request.addParameter("service", "service");
        this.casArgumentExtractor.putTicketGrantingTicketIn(context, this.ticketGrantingTicket);
        
        this.action.execute(context);
        
        assertNotNull(this.casArgumentExtractor.getServiceTicketFrom(context));
    }
    
    public void testTicketGrantingTicketNoTgt() throws Exception {
        MockRequestContext context = new MockRequestContext();
        MockHttpServletRequest request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        request.addParameter("service", "service");
        this.casArgumentExtractor.putTicketGrantingTicketIn(context, "bleh");
        
        assertEquals("error", this.action.execute(context).getId());
    }
    
    public void testTicketGrantingTicketNotTgtButGateway() throws Exception {
        MockRequestContext context = new MockRequestContext();
        MockHttpServletRequest request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        request.addParameter("service", "service");
        request.addParameter("gateway", "true");
        this.casArgumentExtractor.putTicketGrantingTicketIn(context, "bleh");
        
        assertEquals("gateway", this.action.execute(context).getId());
    }
}
