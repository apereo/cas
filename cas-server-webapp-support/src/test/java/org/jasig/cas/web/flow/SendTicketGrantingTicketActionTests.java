/*
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
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

import javax.servlet.http.Cookie;

import org.jasig.cas.AbstractCentralAuthenticationServiceTest;
import org.jasig.cas.web.support.CookieRetrievingCookieGenerator;
import org.jasig.cas.web.support.WebUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.test.MockRequestContext;
import static org.junit.Assert.*;

public class SendTicketGrantingTicketActionTests extends AbstractCentralAuthenticationServiceTest {
    private SendTicketGrantingTicketAction action;

    private CookieRetrievingCookieGenerator ticketGrantingTicketCookieGenerator;

    private MockRequestContext context;

    @Before
    public void onSetUp() throws Exception {
        this.action = new SendTicketGrantingTicketAction();

        this.ticketGrantingTicketCookieGenerator = new CookieRetrievingCookieGenerator();

        this.ticketGrantingTicketCookieGenerator.setCookieName("TGT");

        this.action.setCentralAuthenticationService(getCentralAuthenticationService());

        this.action.setTicketGrantingTicketCookieGenerator(this.ticketGrantingTicketCookieGenerator);

        this.action.afterPropertiesSet();

        this.context = new MockRequestContext();
    }

    @Test
    public void testNoTgtToSet() throws Exception {
        this.context.setExternalContext(new ServletExternalContext(new MockServletContext(),
                new MockHttpServletRequest(), new MockHttpServletResponse()));

        assertEquals("success", this.action.execute(this.context).getId());
    }

    @Test
    public void testTgtToSet() throws Exception {
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final String TICKET_VALUE = "test";

        WebUtils.putTicketGrantingTicketInRequestScope(this.context, TICKET_VALUE);
        this.context.setExternalContext(new ServletExternalContext(new MockServletContext(), new MockHttpServletRequest(), response));

        assertEquals("success", this.action.execute(this.context).getId());
        assertEquals(TICKET_VALUE, response.getCookies()[0].getValue());
    }

    @Test
    public void testTgtToSetRemovingOldTgt() throws Exception {
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockHttpServletRequest request = new MockHttpServletRequest();
        final String TICKET_VALUE = "test";
        request.setCookies(new Cookie[] {new Cookie("TGT", "test5")});
        WebUtils.putTicketGrantingTicketInRequestScope(this.context, TICKET_VALUE);
        this.context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, response));

        assertEquals("success", this.action.execute(this.context).getId());
        assertEquals(TICKET_VALUE, response.getCookies()[0].getValue());
    }
}
