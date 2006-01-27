/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import javax.servlet.http.Cookie;

import org.jasig.cas.AbstractCentralAuthenticationServiceTest;
import org.jasig.cas.TestUtils;
import org.jasig.cas.web.flow.util.ContextUtils;
import org.jasig.cas.web.support.WebConstants;
import org.jasig.cas.web.util.SecureCookieGenerator;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.webflow.execution.servlet.ServletEvent;
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
    
    private SecureCookieGenerator tgtCookieGenerator;
    
    private SecureCookieGenerator warnCookieGenerator;

    protected void onSetUp() throws Exception {
        this.action = new GenerateServiceTicketAction();
        this.tgtCookieGenerator = new SecureCookieGenerator();
        this.tgtCookieGenerator.setCookieName("TGT");
        
        this.warnCookieGenerator = new SecureCookieGenerator();
        this.warnCookieGenerator.setCookieName("WARN");
        
        this.action.setCentralAuthenticationService(getCentralAuthenticationService());
        this.action.setWarnCookieGenerator(new SecureCookieGenerator());
        this.action.setTicketGrantingTicketCookieGenerator(this.tgtCookieGenerator);
        this.action.setWarnCookieGenerator(this.warnCookieGenerator);
        
        this.ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(TestUtils.getCredentialsWithSameUsernameAndPassword());
    }
    
    public void testServiceTicketFromCookie() throws Exception {
        MockRequestContext context = new MockRequestContext();
        MockHttpServletRequest request = new MockHttpServletRequest();
        context.setSourceEvent(new ServletEvent(request, new MockHttpServletResponse()));
        request.addParameter("service", "service");
        request.setCookies(new Cookie[] {new Cookie("TGT", this.ticketGrantingTicket)});
        
        this.action.execute(context);
        
        assertNotNull(ContextUtils.getAttribute(context, WebConstants.TICKET));
    }
    
    public void testTicketGrantingTicketFromRequest() throws Exception {
        MockRequestContext context = new MockRequestContext();
        MockHttpServletRequest request = new MockHttpServletRequest();
        context.setSourceEvent(new ServletEvent(request, new MockHttpServletResponse()));
        request.addParameter("service", "service");
        ContextUtils.addAttribute(context, AbstractLoginAction.REQUEST_ATTRIBUTE_TICKET_GRANTING_TICKET, this.ticketGrantingTicket);
        
        this.action.execute(context);
        
        assertNotNull(ContextUtils.getAttribute(context, WebConstants.TICKET));
    }
    
    public void testTicketGrantingTicketNoTgt() throws Exception {
        MockRequestContext context = new MockRequestContext();
        MockHttpServletRequest request = new MockHttpServletRequest();
        context.setSourceEvent(new ServletEvent(request, new MockHttpServletResponse()));
        request.addParameter("service", "service");
        ContextUtils.addAttribute(context, AbstractLoginAction.REQUEST_ATTRIBUTE_TICKET_GRANTING_TICKET, "bleh");
        
        assertEquals("error", this.action.execute(context).getId());
    }
    
    public void testTicketGrantingTicketNotTgtButGateway() throws Exception {
        MockRequestContext context = new MockRequestContext();
        MockHttpServletRequest request = new MockHttpServletRequest();
        context.setSourceEvent(new ServletEvent(request, new MockHttpServletResponse()));
        request.addParameter("service", "service");
        request.addParameter("gateway", "true");
        ContextUtils.addAttribute(context, AbstractLoginAction.REQUEST_ATTRIBUTE_TICKET_GRANTING_TICKET, "bleh");
        
        assertEquals("gateway", this.action.execute(context).getId());
    }
    
    

}
