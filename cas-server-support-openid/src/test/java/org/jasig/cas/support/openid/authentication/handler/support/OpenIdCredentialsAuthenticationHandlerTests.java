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
package org.jasig.cas.support.openid.authentication.handler.support;

import static org.junit.Assert.*;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.MutableAuthentication;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.support.openid.authentication.principal.OpenIdCredentials;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.registry.DefaultTicketRegistry;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Scott Battaglia
 * @since 3.1
 */
public class OpenIdCredentialsAuthenticationHandlerTests {

    private OpenIdCredentialsAuthenticationHandler openIdCredentialsAuthenticationHandler;

    private TicketRegistry ticketRegistry;

    @Before
    public void setUp() throws Exception {
        this.openIdCredentialsAuthenticationHandler = new OpenIdCredentialsAuthenticationHandler();
        this.ticketRegistry = new DefaultTicketRegistry();
        this.openIdCredentialsAuthenticationHandler.setTicketRegistry(this.ticketRegistry);
    }

    @Test
    public void testSupports() {
        assertTrue(this.openIdCredentialsAuthenticationHandler.supports(new OpenIdCredentials("test", "test")));
        assertFalse(this.openIdCredentialsAuthenticationHandler.supports(new UsernamePasswordCredentials()));
    }

    @Test
    public void testTGTWithSameId() throws Exception {
        final OpenIdCredentials c = new OpenIdCredentials("test", "test");
        final TicketGrantingTicket t = getTicketGrantingTicket();
        this.ticketRegistry.addTicket(t);

        assertTrue(this.openIdCredentialsAuthenticationHandler.authenticate(c));
    }

    @Test
    public void testTGTThatIsExpired() throws Exception {
        final OpenIdCredentials c = new OpenIdCredentials("test", "test");
        final TicketGrantingTicket t = getTicketGrantingTicket();
        this.ticketRegistry.addTicket(t);

        t.markTicketExpired();
        assertFalse(this.openIdCredentialsAuthenticationHandler.authenticate(c));
    }

    @Test
    public void testTGTWithDifferentId() throws Exception {
        final OpenIdCredentials c = new OpenIdCredentials("test", "test1");
        final TicketGrantingTicket t = getTicketGrantingTicket();
        this.ticketRegistry.addTicket(t);

        assertFalse(this.openIdCredentialsAuthenticationHandler.authenticate(c));
    }

    protected TicketGrantingTicket getTicketGrantingTicket() {
        final Authentication authentication = new MutableAuthentication(new SimplePrincipal("test"));
        return new TicketGrantingTicketImpl("test", authentication, new NeverExpiresExpirationPolicy());
    }
}
