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
import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;

import junit.framework.TestCase;

import java.util.concurrent.TimeUnit;

/**
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 3.0
 */
public class MultiTimeUseOrTimeoutExpirationPolicyTests extends TestCase {

    private static final long TIMEOUT_SECONDS = 5L;

    private static final long TIMEOUT_MILLISECONDS = 5000L;

    private static final int NUMBER_OF_USES = 5;
    
    private static final int TIMEOUT_BUFFER = 100;

    private ExpirationPolicy expirationPolicy;

    private TicketGrantingTicket ticket;

    protected void setUp() throws Exception {
        this.expirationPolicy = new MultiTimeUseOrTimeoutExpirationPolicy(
            NUMBER_OF_USES, TIMEOUT_SECONDS, TimeUnit.SECONDS);

        this.ticket = new TicketGrantingTicketImpl("test", TestUtils
            .getAuthentication(), this.expirationPolicy);

        super.setUp();
    }

    public void testTicketIsNull() {
        assertTrue(this.expirationPolicy.isExpired(null));
    }

    public void testTicketIsNotExpired() {
        assertFalse(this.ticket.isExpired());
    }

    public void testTicketIsExpiredByTime() {
        try {
            Thread.sleep(TIMEOUT_MILLISECONDS + TIMEOUT_BUFFER);
            assertTrue(this.ticket.isExpired());
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }

    public void testTicketIsExpiredByCount() {
        for (int i = 0; i < NUMBER_OF_USES; i++)
            this.ticket.grantServiceTicket("test", TestUtils.getService(), new NeverExpiresExpirationPolicy(), false);

        assertTrue(this.ticket.isExpired());
    }
}