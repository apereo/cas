/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import javax.servlet.http.Cookie;

import org.jasig.cas.AbstractCentralAuthenticationServiceTest;
import org.jasig.cas.web.flow.util.ContextUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;



public class SendTicketGrantingTicketActionTests extends AbstractCentralAuthenticationServiceTest {
    private SendTicketGrantingTicketAction action;
    
    private CookieGenerator warnCookieGenerator;
    
    private CookieGenerator ticketGrantingTicketCookieGenerator;
    
    private MockRequestContext context;

    protected void onSetUp() throws Exception {
        this.action = new SendTicketGrantingTicketAction();
        
        this.warnCookieGenerator = new CookieGenerator();
        this.ticketGrantingTicketCookieGenerator = new CookieGenerator();
        
        this.ticketGrantingTicketCookieGenerator.setCookieName("TGT");
        this.warnCookieGenerator.setCookieName("WARN");
        
        this.action.setCentralAuthenticationService(getCentralAuthenticationService());
        this.action.setTicketGrantingTicketCookieGenerator(this.ticketGrantingTicketCookieGenerator);
        this.action.setWarnCookieGenerator(this.warnCookieGenerator);
        
        this.action.afterPropertiesSet();
        
        this.context = new MockRequestContext();
    }
    
    public void testNoTgtToSet() throws Exception {
        this.context.setExternalContext(new ServletExternalContext(new MockServletContext(), new MockHttpServletRequest(), new MockHttpServletResponse()));
        
        assertEquals("success", this.action.execute(this.context).getId());
    }
    
    public void testTgtToSet() throws Exception {
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final String TICKET_VALUE = "test";
        ContextUtils.addAttribute(this.context, AbstractLoginAction.REQUEST_ATTRIBUTE_TICKET_GRANTING_TICKET, TICKET_VALUE);
        this.context.setExternalContext(new ServletExternalContext(new MockServletContext(), new MockHttpServletRequest(), response));
        
        assertEquals("success", this.action.execute(this.context).getId());
        assertEquals(TICKET_VALUE, response.getCookies()[0].getValue());
    }
    
    public void testTgtToSetRemovingOldTgt() throws Exception {
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final String TICKET_VALUE = "test";
        request.setCookies(new Cookie[] {new Cookie("TGT", "test5")});
        ContextUtils.addAttribute(this.context, AbstractLoginAction.REQUEST_ATTRIBUTE_TICKET_GRANTING_TICKET, TICKET_VALUE);
        this.context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        
        assertEquals("success", this.action.execute(this.context).getId());
        assertEquals(TICKET_VALUE, response.getCookies()[0].getValue());
    }
    
    
    
 
    
    
}
