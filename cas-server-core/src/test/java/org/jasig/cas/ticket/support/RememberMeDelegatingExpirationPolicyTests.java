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
