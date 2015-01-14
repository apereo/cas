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
package org.jasig.cas.web.flow;

import org.jasig.cas.AbstractCentralAuthenticationServiceTest;
import org.jasig.cas.TestUtils;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.web.support.WebUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;

import javax.servlet.http.Cookie;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0.4
 */
public final class GenerateServiceTicketActionTests extends AbstractCentralAuthenticationServiceTest {

    private GenerateServiceTicketAction action;

    private TicketGrantingTicket ticketGrantingTicket;

    @Before
    public void onSetUp() throws Exception {
        this.action = new GenerateServiceTicketAction();
        this.action.setCentralAuthenticationService(getCentralAuthenticationService());
        this.action.afterPropertiesSet();

        this.ticketGrantingTicket = getCentralAuthenticationService().createTicketGrantingTicket(
                TestUtils.getCredentialsWithSameUsernameAndPassword());
    }

    @Test
    public void verifyServiceTicketFromCookie() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        context.getFlowScope().put("service", TestUtils.getService());
        context.getFlowScope().put("ticketGrantingTicketId", this.ticketGrantingTicket.getId());
        final MockHttpServletRequest request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(
                new MockServletContext(), request, new MockHttpServletResponse()));
        request.addParameter("service", "service");
        request.setCookies(new Cookie("TGT", this.ticketGrantingTicket.getId()));

        this.action.execute(context);

        assertNotNull(WebUtils.getServiceTicketFromRequestScope(context));
    }

    @Test
    public void verifyTicketGrantingTicketFromRequest() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        context.getFlowScope().put("service", TestUtils.getService());
        final MockHttpServletRequest request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(
                new MockServletContext(), request, new MockHttpServletResponse()));
        request.addParameter("service", "service");
        WebUtils.putTicketGrantingTicketInScopes(context,
                this.ticketGrantingTicket);

        this.action.execute(context);

        assertNotNull(WebUtils.getServiceTicketFromRequestScope(context));
    }

    @Test
    public void verifyTicketGrantingTicketNoTgt() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        context.getFlowScope().put("service", TestUtils.getService());
        
        final MockHttpServletRequest request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(
                new MockServletContext(), request, new MockHttpServletResponse()));
        request.addParameter("service", "service");

        final TicketGrantingTicket tgt = mock(TicketGrantingTicket.class);
        when(tgt.getId()).thenReturn("bleh");
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);

        assertEquals("error", this.action.execute(context).getId());
    }

    @Test
    public void verifyTicketGrantingTicketExpiredTgt() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        context.getFlowScope().put("service", TestUtils.getService());
        final MockHttpServletRequest request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(
                new MockServletContext(), request, new MockHttpServletResponse()));
        request.addParameter("service", "service");
        WebUtils.putTicketGrantingTicketInScopes(context, this.ticketGrantingTicket);

        this.ticketGrantingTicket.markTicketExpired();
        assertEquals("error", this.action.execute(context).getId());
    }
    
    @Test
    public void verifyTicketGrantingTicketNotTgtButGateway() throws Exception {
        final MockRequestContext context = new MockRequestContext();
        context.getFlowScope().put("service", TestUtils.getService());
        final MockHttpServletRequest request = new MockHttpServletRequest();
        context.setExternalContext(new ServletExternalContext(
                new MockServletContext(), request, new MockHttpServletResponse()));
        request.addParameter("service", "service");
        request.addParameter("gateway", "true");
        final TicketGrantingTicket tgt = mock(TicketGrantingTicket.class);
        when(tgt.getId()).thenReturn("bleh");
        WebUtils.putTicketGrantingTicketInScopes(context, tgt);


        assertEquals("gateway", this.action.execute(context).getId());
    }
}
