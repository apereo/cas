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
package org.jasig.cas.web;

import java.util.Map;

import org.jasig.cas.AbstractCentralAuthenticationServiceTest;
import org.jasig.cas.TestUtils;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import static org.junit.Assert.*;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class ProxyControllerTests extends AbstractCentralAuthenticationServiceTest {

    private ProxyController proxyController;

    @Before
    public void onSetUp() throws Exception {
        this.proxyController = new ProxyController();
        this.proxyController
        .setCentralAuthenticationService(getCentralAuthenticationService());

        final StaticApplicationContext context = new StaticApplicationContext();
        context.refresh();
        this.proxyController.setApplicationContext(context);
    }

    @Test
    public void verifyNoParams() throws Exception {
        assertEquals("INVALID_REQUEST", this.proxyController
                .handleRequestInternal(new MockHttpServletRequest(),
                        new MockHttpServletResponse()).getModel()
                        .get("code"));
    }

    @Test
    public void verifyNonExistentPGT() throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("pgt", "TestService");
        request.addParameter("targetService", "testDefault");

        assertTrue(this.proxyController.handleRequestInternal(request,
                new MockHttpServletResponse()).getModel().containsKey(
                        "code"));
    }

    @Test
    public void verifyExistingPGT() throws Exception {
        final TicketGrantingTicket ticket = new TicketGrantingTicketImpl(
                "ticketGrantingTicketId", TestUtils.getAuthentication(),
                new NeverExpiresExpirationPolicy());
        getTicketRegistry().addTicket(ticket);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request
        .addParameter("pgt", ticket.getId());
        request.addParameter(
                "targetService", "testDefault");

        assertTrue(this.proxyController.handleRequestInternal(request,
                new MockHttpServletResponse()).getModel().containsKey(
                        "ticket"));
    }

    @Test
    public void verifyNotAuthorizedPGT() throws Exception {
        final TicketGrantingTicket ticket = new TicketGrantingTicketImpl("ticketGrantingTicketId", TestUtils.getAuthentication(),
                new NeverExpiresExpirationPolicy());
        getTicketRegistry().addTicket(ticket);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.addParameter("pgt", ticket.getId());
        request.addParameter("targetService", "service");

        final Map<String, Object> map = this.proxyController.handleRequestInternal(request,  new MockHttpServletResponse()).getModel();
        assertTrue(!map.containsKey("ticket"));
    }
}
