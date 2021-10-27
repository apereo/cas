package org.jasig.cas.ticket.support;

import static org.junit.Assert.*;

import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class ThrottledUseAndTimeoutExpirationPolicyTests  {

    private static final long TIMEOUT = 50;

    private static final long TIMEOUT_BUFFER = 10;

    private ThrottledUseAndTimeoutExpirationPolicy expirationPolicy;

    private TicketGrantingTicket ticket;

    @Before
    public void setUp() throws Exception {
        this.expirationPolicy = new ThrottledUseAndTimeoutExpirationPolicy();
        this.expirationPolicy.setTimeToKillInMilliSeconds(TIMEOUT);
        this.expirationPolicy.setTimeInBetweenUsesInMilliSeconds(TIMEOUT / 5);

        this.ticket = new TicketGrantingTicketImpl("test", org.jasig.cas.authentication.TestUtils
            .getAuthentication(), this.expirationPolicy);

    }

    @Test
    public void verifyTicketIsNotExpired() {
        assertFalse(this.ticket.isExpired());
    }

    @Test
    public void verifyTicketIsExpired() throws InterruptedException {
        Thread.sleep(TIMEOUT + TIMEOUT_BUFFER);
        assertTrue(this.ticket.isExpired());
    }

    @Test
    public void verifyTicketUsedButWithTimeout() throws InterruptedException {
        this.ticket.grantServiceTicket("test", org.jasig.cas.services.TestUtils.getService(), this.expirationPolicy, false,
                true);
        Thread.sleep(TIMEOUT - TIMEOUT_BUFFER);
        assertFalse(this.ticket.isExpired());
    }

    @Test
    public void verifyNotWaitingEnoughTime() {
        this.ticket.grantServiceTicket("test", org.jasig.cas.services.TestUtils.getService(), this.expirationPolicy, false,
                true);
        assertTrue(this.ticket.isExpired());
    }
}
