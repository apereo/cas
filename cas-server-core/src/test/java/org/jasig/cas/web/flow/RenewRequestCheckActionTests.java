/*
 * Copyright 2005 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.ja-sig.org/products/cas/overview/license/
 */
package org.jasig.cas.web.flow;

import org.jasig.cas.web.support.ArgumentExtractor;
import org.jasig.cas.web.support.CasArgumentExtractor;
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
public class RenewRequestCheckActionTests extends TestCase {

    private RenewRequestCheckAction action = new RenewRequestCheckAction();

    protected void setUp() throws Exception {
        this.action
            .setTicketGrantingTicketCookieGenerator(new CookieGenerator());
        this.action
            .setArgumentExtractors(new ArgumentExtractor[] {new CasArgumentExtractor()});
        this.action.afterPropertiesSet();
    }

    public void testRenewIsTrue() {
        final MockRequestContext mockRequestContext = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("service", "service");
        request.setParameter("renew", "true");

        mockRequestContext.setExternalContext(new ServletExternalContext(
            new MockServletContext(), request, new MockHttpServletResponse()));

        assertEquals("authenticationRequired", this.action.doExecute(
            mockRequestContext).getId());
    }

    public void testRenewIsFalse() {
        final MockRequestContext mockRequestContext = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("service", "service");

        mockRequestContext.setExternalContext(new ServletExternalContext(
            new MockServletContext(), request, new MockHttpServletResponse()));

        assertEquals("generateServiceTicket", this.action.doExecute(
            mockRequestContext).getId());
    }

}
