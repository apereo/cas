package org.jasig.cas.ticket.support;

import static org.junit.Assert.*;

import org.jasig.cas.ticket.ExpirationPolicy;
import org.jasig.cas.ticket.TicketGrantingTicket;
import org.jasig.cas.ticket.TicketGrantingTicketImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author Scott Battaglia
 * @since 3.0.0
 */
public class MultiTimeUseOrTimeoutExpirationPolicyTests {

    private static final long TIMEOUT_MILLISECONDS = 100L;

    private static final int NUMBER_OF_USES = 5;

    private static final int TIMEOUT_BUFFER = 50;

    private ExpirationPolicy expirationPolicy;

    private TicketGrantingTicket ticket;

    @Before
    public void setUp() throws Exception {
        this.expirationPolicy = new MultiTimeUseOrTimeoutExpirationPolicy(NUMBER_OF_USES, TIMEOUT_MILLISECONDS,
                TimeUnit.MILLISECONDS);

        this.ticket = new TicketGrantingTicketImpl("test",
                org.jasig.cas.authentication.TestUtils.getAuthentication(), this.expirationPolicy);

    }

    @Test
    public void verifyTicketIsNull() {
        assertTrue(this.expirationPolicy.isExpired(null));
    }

    @Test
    public void verifyTicketIsNotExpired() {
        assertFalse(this.ticket.isExpired());
    }

    @Test
    public void verifyTicketIsExpiredByTime() throws InterruptedException {
            Thread.sleep(TIMEOUT_MILLISECONDS + TIMEOUT_BUFFER);
            assertTrue(this.ticket.isExpired());
    }

    @Test
    public void verifyTicketIsExpiredByCount() {
        for (int i = 0; i < NUMBER_OF_USES; i++) {
            this.ticket.grantServiceTicket("test",
                    org.jasig.cas.services.TestUtils.getService(), new NeverExpiresExpirationPolicy(), false,
                    true);
        }
        assertTrue(this.ticket.isExpired());
    }
}
