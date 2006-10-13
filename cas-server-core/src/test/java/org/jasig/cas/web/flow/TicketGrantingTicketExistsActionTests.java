/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web.flow;

import javax.servlet.http.Cookie;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import junit.framework.TestCase;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.4
 */
public class TicketGrantingTicketExistsActionTests extends TestCase {

    private TicketGrantingTicketExistsAction action = new TicketGrantingTicketExistsAction();
    
    private CookieGenerator tgtCookieGenerator = new CookieGenerator();

    protected void setUp() throws Exception {
        this.tgtCookieGenerator.setCookieName("tgt");
        this.action.setTicketGrantingTicketCookieGenerator(this.tgtCookieGenerator);
        this.action.afterPropertiesSet();
    }

    public void testTicketExists() {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        
        request.setCookies(new Cookie[] {new Cookie(this.tgtCookieGenerator.getCookieName(), "test")});
        
        final MockRequestContext requestContext = new MockRequestContext();
        requestContext.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        
        assertEquals("ticketGrantingTicketExists", this.action
            .doExecute(requestContext).getId());
    }

    public void testTicketDoesntExists() {
        final MockRequestContext requestContext = new MockRequestContext();
        requestContext.setExternalContext(new ServletExternalContext(new MockServletContext(), new MockHttpServletRequest(), new MockHttpServletResponse()));
        
        assertEquals("noTicketGrantingTicketExists", this.action
            .doExecute(requestContext).getId());
    }

}
