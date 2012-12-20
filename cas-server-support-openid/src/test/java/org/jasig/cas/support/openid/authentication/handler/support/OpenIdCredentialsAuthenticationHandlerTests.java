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

import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;

import junit.framework.TestCase;
import org.jasig.cas.authentication.MutableAuthentication;
import org.jasig.cas.authentication.SimplePrincipal;
import org.jasig.cas.authentication.UsernamePasswordCredential;
import org.jasig.cas.support.openid.authentication.principal.OpenIdCredential;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.registry.DefaultTicketRegistry;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;

/**
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.1
 *
 */
public class OpenIdCredentialsAuthenticationHandlerTests extends TestCase {

    private OpenIdCredentialsAuthenticationHandler openIdCredentialsAuthenticationHandler;
    
    private TicketRegistry ticketRegistry;

    protected void setUp() throws Exception {
        super.setUp();
        this.openIdCredentialsAuthenticationHandler = new OpenIdCredentialsAuthenticationHandler();
        this.ticketRegistry = new DefaultTicketRegistry();
        this.openIdCredentialsAuthenticationHandler.setTicketRegistry(this.ticketRegistry);
    }
    
    public void testSupports() {
        assertTrue(this.openIdCredentialsAuthenticationHandler.supports(new OpenIdCredential("test", "test")));
        assertFalse(this.openIdCredentialsAuthenticationHandler.supports(new UsernamePasswordCredential()));
    }
    
    public void testTGTWithSameId() throws Exception {
        final OpenIdCredential c = new OpenIdCredential("test", "test");
        final TicketGrantingTicket t = getTicketGrantingTicket();
        this.ticketRegistry.addTicket(t);
        
        assertEquals("test", this.openIdCredentialsAuthenticationHandler.authenticate(c).getPrincipal().getId());
    }
    
    public void testTGTThatIsExpired() throws Exception {
        final OpenIdCredential c = new OpenIdCredential("test", "test");
        final TicketGrantingTicket t = getTicketGrantingTicket();
        this.ticketRegistry.addTicket(t);
        
        t.expire();
        try {
            this.openIdCredentialsAuthenticationHandler.authenticate(c);
            fail("Should have thrown CredentialExpiredException");
        } catch (CredentialExpiredException e) {}
    }
    
    public void testTGTWithDifferentId() throws Exception {
        final OpenIdCredential c = new OpenIdCredential("test", "test1");
        final TicketGrantingTicket t = getTicketGrantingTicket();
        this.ticketRegistry.addTicket(t);

        try {
            this.openIdCredentialsAuthenticationHandler.authenticate(c);
            fail("Should have thrown CredentialExpiredException");
        } catch (FailedLoginException e) {}
    }
    
    protected TicketGrantingTicket getTicketGrantingTicket() {
        final MutableAuthentication authentication = new MutableAuthentication();
        authentication.setPrincipal(new SimplePrincipal("test"));
        return new TicketGrantingTicketImpl("test", authentication, new NeverExpiresExpirationPolicy());
    }
}
