/*
 * Copyright 2007 The JA-SIG Collaborative. All rights reserved. See license
 * distributed with this file and available online at
 * http://www.uportal.org/license.html
 */
package org.jasig.cas.ticket.support;

import org.jasig.cas.TestUtils;
import org.jasig.cas.authentication.MutableAuthentication;
import org.jasig.cas.authentication.principal.RememberMeCredentials;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;

import junit.framework.TestCase;

/**
 * Tests for RememberMeDelegatingExpirationPolicy
 * 
 * @author Scott Battaglia
 * @version $Revision: 1.1 $ $Date: 2005/08/19 18:27:17 $
 * @since 3.2.1
 *
 */
public final class RememberMeDelegatingExpirationPolicyTests extends TestCase {

    private RememberMeDelegatingExpirationPolicy p;

    protected void setUp() throws Exception {
        this.p = new RememberMeDelegatingExpirationPolicy();
        this.p.setRememberMeExpirationPolicy(new MultiTimeUseOrTimeoutExpirationPolicy(1, 20000));
        this.p.setSessionExpirationPolicy(new MultiTimeUseOrTimeoutExpirationPolicy(5, 20000));
    }

    public void testTicketExpirationWithRememberMe() {
        final MutableAuthentication authentication = new MutableAuthentication(TestUtils.getPrincipal());
        authentication.getAttributes().put(RememberMeCredentials.AUTHENTICATION_ATTRIBUTE_REMEMBER_ME, Boolean.TRUE);
        final TicketGrantingTicketImpl t = new TicketGrantingTicketImpl("111", authentication, this.p);
        assertFalse(t.isExpired());
        t.grantServiceTicket("55", TestUtils.getService(), this.p, false);
        assertTrue(t.isExpired());
        
    }
    
    public void testTicketExpirationWithoutRememberMe() {
        final MutableAuthentication authentication = new MutableAuthentication(TestUtils.getPrincipal());
        final TicketGrantingTicketImpl t = new TicketGrantingTicketImpl("111", authentication, this.p);
        assertFalse(t.isExpired());
        t.grantServiceTicket("55", TestUtils.getService(), this.p, false);
        assertFalse(t.isExpired());
        
    }
    
}
