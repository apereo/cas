/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.web.flow;

import javax.servlet.http.Cookie;

import org.jasig.cas.AbstractCentralAuthenticationServiceTest;
import org.jasig.cas.TestUtils;
import org.jasig.cas.web.support.WebConstants;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class TicketGrantingTicketCheckActionTests extends
    AbstractCentralAuthenticationServiceTest {

    private TicketGrantingTicketCheckAction checkAction = new TicketGrantingTicketCheckAction();

    protected void onSetUp() throws Exception {
        this.checkAction
            .setCentralAuthenticationService(getCentralAuthenticationService());
        this.checkAction.afterPropertiesSet();
    }

    public void testGatewayNoService() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(WebConstants.GATEWAY, "test");

        assertEquals("error", this.checkAction.doExecute(
            TestUtils.getContext(request)).getId());
    }

    public void testCheckNoTicketGrantingTicketCookie() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(WebConstants.SERVICE, "test");
        assertEquals("error", this.checkAction.doExecute(
            TestUtils.getContext(request)).getId());
    }

    public void testCheckInvalidTicketGrantingTicketCookie() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(WebConstants.SERVICE, "test");
        Cookie cookie = new Cookie(WebConstants.COOKIE_TGC_ID, "test");
        request.setCookies(new Cookie[] {cookie});

        assertEquals("error", this.checkAction.doExecute(
            TestUtils.getContext(request)).getId());
    }

    public void testCheckInvalidTicketGrantingTicketCookieWithGateway()
        throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(WebConstants.SERVICE, "test");
        request.addParameter(WebConstants.GATEWAY, "true");
        Cookie cookie = new Cookie(WebConstants.COOKIE_TGC_ID, "test");
        request.setCookies(new Cookie[] {cookie});

        assertEquals("gateway", this.checkAction.doExecute(
            TestUtils.getContext(request)).getId());
    }

    public void testCheckRenewTrue() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(WebConstants.RENEW, "true");
        request.addParameter(WebConstants.SERVICE, "test");
        assertEquals("error", this.checkAction.doExecute(
            TestUtils.getContext(request)).getId());
    }

    public void testCheckValidTicketGrantingTicketCookie() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final Cookie cookie = new Cookie(WebConstants.COOKIE_TGC_ID,
            getCentralAuthenticationService().createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword()));
        request.addParameter(WebConstants.SERVICE, "test");
        request.setCookies(new Cookie[] {cookie});
        assertEquals("success", this.checkAction.doExecute(
            TestUtils.getContext(request)).getId());
    }

    public void testValidTgtNoService() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final Cookie cookie = new Cookie(WebConstants.COOKIE_TGC_ID,
            getCentralAuthenticationService().createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword()));
        request.setCookies(new Cookie[] {cookie});

        assertEquals("noService", this.checkAction.doExecute(
            TestUtils.getContext(request)).getId());
    }

    public void testNoTgtAndGateway() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();

        request.addParameter(WebConstants.GATEWAY, "true");
        request.addParameter(WebConstants.SERVICE, "test");

        assertEquals("gateway", this.checkAction.doExecute(
            TestUtils.getContext(request)).getId());
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
