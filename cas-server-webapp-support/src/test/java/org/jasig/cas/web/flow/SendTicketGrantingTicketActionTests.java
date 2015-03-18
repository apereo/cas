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
import org.jasig.cas.CasProtocolConstants;
import org.jasig.cas.authentication.principal.WebApplicationService;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.web.support.CookieRetrievingCookieGenerator;
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
 * @author Marvin S. Addison
 * @since 3.4.0
 */
public class SendTicketGrantingTicketActionTests extends AbstractCentralAuthenticationServiceTest {
    private SendTicketGrantingTicketAction action;
    private CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;
    private MockRequestContext context;

    @Before
    public void onSetUp() throws Exception {

        this.ticketGrantingTicketCookieGenerator = new CookieRetrievingCookieGenerator();
        ticketGrantingTicketCookieGenerator.setCookieName("TGT");

        this.action = new SendTicketGrantingTicketAction(ticketGrantingTicketCookieGenerator,
                getCentralAuthenticationService(), getServicesManager());
        this.action.setServicesManager(getServicesManager());
        this.action.setCreateSsoSessionCookieOnRenewAuthentications(true);
        this.action.afterPropertiesSet();

        this.context = new MockRequestContext();
    }

    @Test
    public void verifyNoTgtToSet() throws Exception {
        this.context.setExternalContext(new ServletExternalContext(new MockServletContext(),
                new MockHttpServletRequest(), new MockHttpServletResponse()));

        assertEquals("success", this.action.execute(this.context).getId());
    }

    @Test
    public void verifyTgtToSet() throws Exception {
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final TicketGrantingTicket tgt = mock(TicketGrantingTicket.class);
        when(tgt.getId()).thenReturn("test");

        WebUtils.putTicketGrantingTicketInScopes(this.context, tgt);
        this.context.setExternalContext(new ServletExternalContext(new MockServletContext(),
                request, response));

        assertEquals("success", this.action.execute(this.context).getId());
        request.setCookies(response.getCookies());
        assertEquals(tgt.getId(), this.ticketGrantingTicketCookieGenerator.retrieveCookieValue(request));
    }

    @Test
    public void verifyTgtToSetRemovingOldTgt() throws Exception {
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockHttpServletRequest request = new MockHttpServletRequest();

        final TicketGrantingTicket tgt = mock(TicketGrantingTicket.class);
        when(tgt.getId()).thenReturn("test");

        request.setCookies(new Cookie("TGT", "test5"));
        WebUtils.putTicketGrantingTicketInScopes(this.context, tgt);
        this.context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        assertEquals("success", this.action.execute(this.context).getId());
        request.setCookies(response.getCookies());
        assertEquals(tgt.getId(), this.ticketGrantingTicketCookieGenerator.retrieveCookieValue(request));
    }

    @Test
    public void verifySsoSessionCookieOnRenewAsParameter() throws Exception {
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter(CasProtocolConstants.PARAMETER_RENEW, "true");

        final TicketGrantingTicket tgt = mock(TicketGrantingTicket.class);
        when(tgt.getId()).thenReturn("test");
        request.setCookies(new Cookie("TGT", "test5"));
        WebUtils.putTicketGrantingTicketInScopes(this.context, tgt);
        this.context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        this.action.setCreateSsoSessionCookieOnRenewAuthentications(false);
        assertEquals("success", this.action.execute(this.context).getId());
        assertEquals(0, response.getCookies().length);
    }

    @Test
    public void verifySsoSessionCookieOnServiceSsoDisallowed() throws Exception {
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockHttpServletRequest request = new MockHttpServletRequest();

        final WebApplicationService svc = mock(WebApplicationService.class);
        when(svc.getId()).thenReturn("TestSsoFalse");

        final TicketGrantingTicket tgt = mock(TicketGrantingTicket.class);
        when(tgt.getId()).thenReturn("test");
        request.setCookies(new Cookie("TGT", "test5"));
        WebUtils.putTicketGrantingTicketInScopes(this.context, tgt);
        this.context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));
        this.context.getFlowScope().put("service", svc);
        this.action.setCreateSsoSessionCookieOnRenewAuthentications(false);
        assertEquals("success", this.action.execute(this.context).getId());
        assertEquals(0, response.getCookies().length);
    }
}
