/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web.flow;

import javax.servlet.http.Cookie;

import org.jasig.cas.AbstractCentralAuthenticationServiceTest;
import org.jasig.cas.TestUtils;
import org.jasig.cas.web.support.ArgumentExtractor;
import org.jasig.cas.web.support.CasArgumentExtractor;
import org.jasig.cas.web.support.WebUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.util.CookieGenerator;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0.4
 */
public final class GenerateServiceTicketActionTests extends
    AbstractCentralAuthenticationServiceTest {

    private GenerateServiceTicketAction action;

    private String ticketGrantingTicket;

    private CookieGenerator tgtCookieGenerator;

    protected void onSetUp() throws Exception {
        this.action = new GenerateServiceTicketAction();
        this.tgtCookieGenerator = new CookieGenerator();
        this.tgtCookieGenerator.setCookieName("TGT");

        this.action
            .setCentralAuthenticationService(getCentralAuthenticationService());
        this.action
            .setArgumentExtractors(new ArgumentExtractor[] {new CasArgumentExtractor()});
        this.action
            .setTicketGrantingTicketCookieGenerator(this.tgtCookieGenerator);
        this.action.afterPropertiesSet();

        this.ticketGrantingTicket = getCentralAuthenticationService()
            .createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
    }

    public void testServiceTicketFromCookie() throws Exception {
        MockRequestContext context = new MockRequestContext();
        MockHttpServletRequest request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(
            new MockServletContext(), request, new MockHttpServletResponse()));
        request.addParameter("service", "service");
        request.setCookies(new Cookie[] {new Cookie("TGT",
            this.ticketGrantingTicket)});

        this.action.execute(context);

        assertNotNull(WebUtils.getServiceTicketFromRequestScope(context));
    }

    public void testTicketGrantingTicketFromRequest() throws Exception {
        MockRequestContext context = new MockRequestContext();
        MockHttpServletRequest request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(
            new MockServletContext(), request, new MockHttpServletResponse()));
        request.addParameter("service", "service");
        WebUtils.putTicketGrantingTicketInRequestScope(context,
            this.ticketGrantingTicket);

        this.action.execute(context);

        assertNotNull(WebUtils.getServiceTicketFromRequestScope(context));
    }

    public void testTicketGrantingTicketNoTgt() throws Exception {
        MockRequestContext context = new MockRequestContext();
        MockHttpServletRequest request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(
            new MockServletContext(), request, new MockHttpServletResponse()));
        request.addParameter("service", "service");
        WebUtils.putTicketGrantingTicketInRequestScope(context, "bleh");

        assertEquals("error", this.action.execute(context).getId());
    }

    public void testTicketGrantingTicketNotTgtButGateway() throws Exception {
        MockRequestContext context = new MockRequestContext();
        MockHttpServletRequest request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(
            new MockServletContext(), request, new MockHttpServletResponse()));
        request.addParameter("service", "service");
        request.addParameter("gateway", "true");
        WebUtils.putTicketGrantingTicketInRequestScope(context, "bleh");

        assertEquals("gateway", this.action.execute(context).getId());
    }
}
