/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import javax.servlet.http.Cookie;

import org.jasig.cas.AbstractCentralAuthenticationServiceTest;
import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.principal.OpenIdService;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 *
 */
public class OpenIdSingleSignOnActionTests extends AbstractCentralAuthenticationServiceTest {

    private OpenIdSingleSignOnAction action;
    
    private CookieGenerator cookieGenerator;

    protected void onSetUp() throws Exception {
        this.action = new OpenIdSingleSignOnAction();
        this.cookieGenerator = new CookieGenerator();
        this.action.setCentralAuthenticationService(getCentralAuthenticationService());
        this.action.setTicketGrantingTicketCookieGenerator(this.cookieGenerator);
        
        this.cookieGenerator.setCookieName("TGT");
        
        this.action.afterPropertiesSet();
    }
    
    public void testNoTgt() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), new MockHttpServletRequest(), new MockHttpServletResponse()));
        assertEquals("error", this.action.doExecute(context).getId());
    }
    
    public void testNoService() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie[] {new Cookie(this.cookieGenerator.getCookieName(), "value")});
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        assertEquals("error", this.action.doExecute(context).getId());
    }
    
    public void testBadUsername() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("openid.identity", "fablah");
        request.setParameter("openid.return_to", "http://www.cnn.com");
        request.setCookies(new Cookie[] {new Cookie(this.cookieGenerator.getCookieName(), "value")});
        
        final OpenIdService service = OpenIdService.createServiceFrom(request);
        context.getFlowScope().put("service", service);
        
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        assertEquals("badUsername", this.action.doExecute(context).getId());
        
    }
    
    public void testSuccessfulServiceTicket() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("TGT-11", TestUtils.getAuthentication("scootman28"), new NeverExpiresExpirationPolicy());
        
        getTicketRegistry().addTicket(t);
        
        request.setParameter("openid.identity", "http://openid.aol.com/scootman28");
        request.setParameter("openid.return_to", "http://www.cnn.com");

        request.setCookies(new Cookie[] {new Cookie(this.cookieGenerator.getCookieName(), "TGT-11")});
        
        final OpenIdService service = OpenIdService.createServiceFrom(request);
        context.getFlowScope().put("service", service);

        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        assertEquals("success", this.action.doExecute(context).getId());
    }
    
    
}
