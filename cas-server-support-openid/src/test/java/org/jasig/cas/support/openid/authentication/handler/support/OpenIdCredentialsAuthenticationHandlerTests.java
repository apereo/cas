/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.support.openid.authentication.handler.support;

import org.jasig.cas.authentication.Authentication;
import org.jasig.cas.authentication.MutableAuthentication;
import org.jasig.cas.authentication.principal.SimplePrincipal;
import org.jasig.cas.authentication.principal.UsernamePasswordCredentials;
import org.jasig.cas.support.openid.authentication.handler.support.OpenIdCredentialsAuthenticationHandler;
import org.jasig.cas.support.openid.authentication.principal.OpenIdCredentials;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.jasig.cas.ticket.registry.DefaultTicketRegistry;
import org.jasig.cas.ticket.registry.TicketRegistry;
import org.jasig.cas.ticket.support.NeverExpiresExpirationPolicy;


import junit.framework.TestCase;

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
        assertTrue(this.openIdCredentialsAuthenticationHandler.supports(new OpenIdCredentials("test", "test")));
        assertFalse(this.openIdCredentialsAuthenticationHandler.supports(new UsernamePasswordCredentials()));
    }
    
    public void testTGTWithSameId() throws Exception {
        final OpenIdCredentials c = new OpenIdCredentials("test", "test");
        final TicketGrantingTicket t = getTicketGrantingTicket();
        this.ticketRegistry.addTicket(t);
        
        assertTrue(this.openIdCredentialsAuthenticationHandler.authenticate(c));
    }
    
    public void testTGTThatIsExpired() throws Exception {
        final OpenIdCredentials c = new OpenIdCredentials("test", "test");
        final TicketGrantingTicket t = getTicketGrantingTicket();
        this.ticketRegistry.addTicket(t);
        
        t.expire();
        assertFalse(this.openIdCredentialsAuthenticationHandler.authenticate(c));
    }
    
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
