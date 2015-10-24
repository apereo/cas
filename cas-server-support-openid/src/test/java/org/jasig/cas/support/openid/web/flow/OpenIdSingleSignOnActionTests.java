/*
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.cas.support.openid.web.flow;


import org.jasig.cas.CentralAuthenticationServiceImpl;
import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.AuthenticationManager;
import org.jasig.cas.support.openid.AbstractOpenIdTests;
import org.jasig.cas.support.openid.OpenIdProtocolConstants;
import org.jasig.cas.support.openid.authentication.principal.OpenIdService;
import org.jasig.cas.support.openid.authentication.principal.OpenIdServiceFactory;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.1
 */
public class OpenIdSingleSignOnActionTests extends AbstractOpenIdTests {

    @Autowired
    private OpenIdSingleSignOnAction action;

    @Autowired
    private TicketRegistry ticketRegistry;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CentralAuthenticationServiceImpl impl;

    @Test
    public void verifyNoTgt() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(
                new MockServletContext(), new MockHttpServletRequest(),
                new MockHttpServletResponse()));
        assertEquals("error", this.action.execute(context).getId());
    }

    @Test
    public void verifyNoService() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(
                new MockServletContext(), request,
                new MockHttpServletResponse()));
        final Event event = this.action.execute(context);

        assertNotNull(event);

        assertEquals("error", this.action.execute(context).getId());
    }

    @Test
    public void verifyBadUsername() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter(OpenIdProtocolConstants.OPENID_IDENTITY, "fablah");
        request.setParameter(OpenIdProtocolConstants.OPENID_RETURNTO, "http://www.cnn.com");

        final OpenIdServiceFactory factory = new OpenIdServiceFactory();
        final OpenIdService service = factory.createService(request);
        context.getFlowScope().put("service", service);
        context.getFlowScope().put("ticketGrantingTicketId", "tgtId");

        context.setExternalContext(new ServletExternalContext(
                new MockServletContext(), request,
                new MockHttpServletResponse()));
        assertEquals("error", this.action.execute(context).getId());
    }

    @Test
    public void verifySuccessfulServiceTicket() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final Authentication authentication = org.jasig.cas.authentication.TestUtils.getAuthentication("scootman28");
        final TicketGrantingTicket t = new TicketGrantingTicketImpl("TGT-11", authentication,
                new NeverExpiresExpirationPolicy());

        this.ticketRegistry.addTicket(t);

        request.setParameter(OpenIdProtocolConstants.OPENID_IDENTITY, "http://openid.aol.com/scootman28");
        request.setParameter(OpenIdProtocolConstants.OPENID_RETURNTO, "http://www.cnn.com");

        final OpenIdService service = new OpenIdServiceFactory().createService(request);
        context.getFlowScope().put("service", service);
        context.getFlowScope().put("ticketGrantingTicketId", t.getId());

        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request,
                new MockHttpServletResponse()));
        assertEquals("success", this.action.execute(context).getId());
    }
}
